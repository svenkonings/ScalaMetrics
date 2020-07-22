package validator

import java.io.File

import codeAnalysis.analyser.metric._
import codeAnalysis.analyser.{Analyser, Compiler}
import org.eclipse.jgit.diff.DiffEntry.ChangeType._
import org.eclipse.jgit.patch.FileHeader
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathFilterGroup

import scala.collection.mutable
import scala.jdk.CollectionConverters._

class ValidatorBriand(owner: String, name: String, branch: String, dir: File, metrics: List[MetricProducer]) extends ValidatorBase(owner, name, branch, dir) {
  private val compiler = new Compiler

  import compiler.global

  private val fileFaults = mutable.Map[String, mutable.Map[String, Int]]() // Map from filename to map of qual names and faults
  private val metricRunner = new MetricRunner(List()) // Empty metric runner since we don't require metrics

  def run(): Unit = {
    repo.getFaultyCommits.foreachEntry(analyseCommit)
    val results = analyse()
    ResultWriter.writeMethodMetrics(dir, "functionResultsBriand", results)
    ResultWriter.writeObjectAverageMetrics(dir, "objectResultsBriand", results)
  }

  private def analyse(): List[Result] = {
    val analyser = new Analyser(dir.getCanonicalPath, metrics)
    val results = analyser.analyse()
    results.foreach(addFaults)
    results
  }

  private def addFaults(result: Result): Unit = {
    val path = getRelativePath(result)
    val unitFaults = fileFaults.getOrElse(path, mutable.Map())
    val faults = unitFaults.getOrElse(result.name, 0)
    if (faults > 0) {
      result.faults = faults
      result.results.foreach(addFaults)
    }
  }

  private def analyseCommit(commit: RevCommit, faults: Int): Unit = {
    val diffs = getDiffs(commit)
    if (diffs.nonEmpty) analyseDiffs(commit, diffs, faults)
  }

  private def analyseDiffs(commit: RevCommit, diffs: Map[String, FileHeader], faults: Int): Unit = {
    val walk = new TreeWalk(repo.git.getRepository)
    walk.addTree(commit.getTree)
    walk.setRecursive(true)
    walk.setFilter(PathFilterGroup.createFromStrings(diffs.keySet.asJava))
    while (walk.next()) {
      val diff = diffs(walk.getPathString)
      val contents = getContents(walk.getObjectId(0))
      analyseDiff(diff, contents, faults)
    }
  }

  private def analyseDiff(diff: FileHeader, contents: String, faults: Int): Unit = {
    processChange(diff)
    val unitFaults = fileFaults.getOrElseUpdate(diff.getNewPath, mutable.Map())
    val source = Compiler.stringToSource(diff.getNewPath, contents)
    val editList = diff.toEditList.asScala.toList

    def addDiffFaults(result: Result): Unit = {
      val name = result.name
      val pos = result.tree.pos
      val source = pos.source
      val start = source.offsetToLine(pos.start)
      val end = source.offsetToLine(pos.end)
      val containsChange = editList.exists(edit => edit.getEndB >= start && edit.getBeginB <= end)
      if (containsChange) {
        if (unitFaults.contains(name))
          unitFaults(name) += faults
        else
          unitFaults(name) = faults
        result.results.foreach(addDiffFaults)
      }
    }

    val tree = compiler.treeFromSource(source)
    if (tree != null) compiler.ask(() => metricRunner.run(tree) match {
      case Some(result) => addDiffFaults(result)
      case None => println("No metric result", tree)
    })
  }

  private def processChange(diff: FileHeader): Unit = diff.getChangeType match {
    case ADD | DELETE | MODIFY => // Do nothing
    case RENAME => fileFaults.remove(diff.getOldPath) match { // If the map contains a result for the renamed file
      case Some(value) => fileFaults(diff.getNewPath) = value // Move it to the new filename
      case None => // Do nothing
    }
    case COPY => if (fileFaults.contains(diff.getOldPath)) // If the map contains a result for the copied file
    fileFaults(diff.getNewPath) = mutable.Map.from(fileFaults(diff.getOldPath)) // Copy it to the new file
  }
}
