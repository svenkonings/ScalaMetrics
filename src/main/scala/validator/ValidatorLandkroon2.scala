package validator

import java.io.File

import codeAnalysis.analyser.metric._
import codeAnalysis.analyser.{Analyser, Compiler}
import org.eclipse.jgit.patch.FileHeader
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathFilterGroup

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.reflect.internal.util.SourceFile

class ValidatorLandkroon2(owner: String, name: String, branch: String, dir: File, metrics: List[MetricProducer]) extends ValidatorBase(owner, name, branch, dir) {
  private val compiler = new Compiler

  import compiler.global

  private val analysedPaths = mutable.Set[String]()
  private val metricRunner = new MetricRunner(metrics) // Empty metric runner since we don't require metrics

  def run(): Unit = {
    val faults = analyseFaults()
    val others = analyseOthers()
    val results = faults ::: others
    ResultWriter.writeMethodMetrics(dir, results)
    ResultWriter.writeObjectMetrics(dir, results)
  }

  private def analyseFaults(): List[Result] = {
    repo.getFaultyCommits.toList.flatMap { case (commit, faults) => analyseCommit(commit, faults) }
  }

  private def analyseOthers(): List[Result] = {
    val analyser = new Analyser(dir.getCanonicalPath, metrics)
    analyser.scalaFiles = analyser.scalaFiles.filter(source => !analysedPaths.contains(getRelativePath(source)))
    analyser.analyse()
  }

  private def analyseCommit(commit: RevCommit, faults: Int): List[Result] = {
    val diffs = getDiffs(commit)
    analysedPaths ++= diffs.keySet
    if (diffs.nonEmpty) analyseDiffs(commit, diffs, faults) else List()
  }

  private def analyseDiffs(commit: RevCommit, diffs: Map[String, FileHeader], faults: Int): List[Result] = {
    val walk = new TreeWalk(repo.git.getRepository)
    walk.addTree(commit.getTree)
    walk.setRecursive(true)
    walk.setFilter(PathFilterGroup.createFromStrings(diffs.keySet.asJava))

    val results = mutable.ListBuffer[Result]()

    while (walk.next()) {
      val path = walk.getPathString
      val contents = getContents(walk.getObjectId(0))
      val source = Compiler.stringToSource(path, contents)
      val analyse = analyseDiff(diffs(path), source, faults)
      if (analyse != null) results += analyse
    }
    results.toList
  }

  private def analyseDiff(diff: FileHeader, source: SourceFile, faults: Int): Result = {
    val editList = diff.toEditList.asScala.toList

    def addFaults(result: Result): Result = {
      val pos = result.tree.pos
      val source = pos.source
      val start = source.offsetToLine(pos.start)
      val end = source.offsetToLine(pos.end)
      val containsChange = editList.exists(edit => edit.getEndB >= start && edit.getBeginB <= end)
      if (containsChange) {
        result.faults = faults
        result.results.foreach(addFaults)
      }
      result
    }

    val tree = compiler.treeFromLoadedSource(source)
    compiler.ask(() => metricRunner.run(tree) match {
      case Some(value) => addFaults(value)
      case None => null
    })
  }
}
