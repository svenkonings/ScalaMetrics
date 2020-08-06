package validator

import java.io.File
import java.nio.charset.StandardCharsets

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric._
import codeAnalysis.util.FileUtil
import gitclient.git.Repo
import org.eclipse.jgit.diff.Edit
import org.eclipse.jgit.patch.FileHeader
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.{OrTreeFilter, PathSuffixFilter}

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.reflect.internal.util.SourceFile
import scala.util.Using.{resource, resources}

class Validator(owner: String, name: String, branch: String, dir: File, labels: List[String], metrics: List[MetricProducer]) {
  private val repo = new Repo(owner, name, branch, dir, labels)
  private val latestResults = mutable.Map[String, Result]()
  private val faultyPaths = mutable.Set[String]()
  private val faultyResults = mutable.ListBuffer[Result]()
  private val pathfilter = OrTreeFilter.create(
    PathSuffixFilter.create(".scala"),
    PathSuffixFilter.create(".java")
  )

  def run(): Unit = {
    analyseLatestVersion()
    analyseFaultyVersions()

    val briandResults = latestResults.values.toList
    val landkroonResults = faultyResults.toList ::: latestResults.toMap.removedAll(faultyPaths).values.toList

    ResultWriter.writeMethodMetrics(dir, "functionResultsBriand", briandResults)
    ResultWriter.writeObjectMetrics(dir, "objectResultsBriand", briandResults)
    ResultWriter.writeMethodMetrics(dir, "functionResultsLandkroon", landkroonResults)
    ResultWriter.writeObjectMetrics(dir, "objectResultsLandkroon", landkroonResults)
  }

  private def analyseLatestVersion(): Unit = {
    println("Analysing latest version...")
    val headCommit = repo.getHeadCommit
    val sources = mutable.ListBuffer[SourceFile]()
    val metricSources = mutable.Map[String, SourceFile]()
    walkSources(headCommit) { (path, source) =>
      sources += source
      if (path.endsWith(".scala")) metricSources += path -> source
    }
    runMetrics(sources, metricSources) { (path, result) =>
      latestResults += path -> result
    }
  }

  private def analyseFaultyVersions(): Unit = {
    println("Analysing faulty versions...")
    val faultyCommits = repo.getFaultyCommits
    val size = faultyCommits.size
    var current = 0.0
    faultyCommits.foreachEntry { (commit, faults) =>
      analyseFaultyCommit(commit, faults)
      current += 1
      val progress = (current / size) * 100
      println(f"$progress%3.2f%%")
    }
  }

  private def analyseFaultyCommit(commit: RevCommit, faults: Int): Unit = {
    val parent = commit.getParent(0)
    val diffs = getDiffs(parent, commit)
    if (diffs.nonEmpty) {
      analyseFaultyDiffs(parent, diffs, faults)
    }
  }

  private def analyseFaultyDiffs(commit: RevCommit, diffs: Map[String, FileHeader], faults: Int): Unit = {
    val sources = mutable.ListBuffer[SourceFile]()
    val metricSources = mutable.Map[String, SourceFile]()
    walkSources(commit) { (path, source) =>
      sources += source
      if (diffs.contains(path)) metricSources += path -> source
    }
    runMetrics(sources, metricSources) { (path, result) =>
      val editList = diffs(path).toEditList.asScala.toList
      analyseFaultyResult(path, result, editList, faults)
    }
  }

  private def analyseFaultyResult(path: String, result: Result, editList: List[Edit], faults: Int): Unit = {
    val latestResult = latestResults(path)

    def addFaults(result: Result): Unit = {
      val start = result.startLine
      val end = result.endLine
      val containsChange = editList.exists(edit => edit.getEndA >= start && edit.getBeginA <= end)
      if (containsChange) {
        result.faults += faults

        def addFaultsToLatest(latestResult: Result): Boolean = {
          if (result.name.equals(latestResult.name)) {
            latestResult.faults += faults
            true
          } else {
            latestResult.results.exists(addFaultsToLatest)
          }
        }

        addFaultsToLatest(latestResult)
        result.results.foreach(addFaults)
      }
    }

    addFaults(result)
    faultyPaths += path
    faultyResults += result
  }

  private def walkSources(commit: RevCommit)(f: (String, SourceFile) => Unit): Unit =
    resources(new TreeWalk(repo.git.getRepository), repo.git.getRepository.newObjectReader()) { (walk, reader) =>
      walk.addTree(commit.getTree)
      walk.setRecursive(true)
      walk.setFilter(pathfilter)
      while (walk.next()) {
        val path = walk.getPathString
        if (!FileUtil.isTestPath(path)) {
          val contents = new String(reader.open(walk.getObjectId(0)).getCachedBytes, StandardCharsets.UTF_8)
          val source = Compiler.stringToSource(path, contents)
          f(path, source)
        }
      }
    }

  def runMetrics(sources: mutable.ListBuffer[SourceFile], metricSources: mutable.Map[String, SourceFile])
                (f: (String, Result) => Unit): Unit = {
    resource(new Compiler) { compiler =>
      compiler.loadSources(sources.toList)
      val metricRunner = compiler.ask(() => new MetricRunner(metrics)(compiler.global))
      metricSources.foreachEntry { (path, source) =>
        val tree = compiler.treeFromLoadedSource(source)
        if (tree != null) {
          val result = compiler.ask(() => metricRunner.run(tree)).get
          f(path, result)
        }
      }
    }
  }

  private def getDiffs(from: RevCommit, to: RevCommit): Map[String, FileHeader] = {
    repo.diff(from.getTree, to.getTree)
      .filter(diff => latestResults.contains(diff.getOldPath)) // Does not take into account renames
      .map(diff => diff.getOldPath -> diff)
      .toMap
  }
}
