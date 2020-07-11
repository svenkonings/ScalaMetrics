package validator

import java.io.File
import java.nio.charset.StandardCharsets

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{Metric, MetricRunner, Result}
import gitclient.git.Repo
import org.eclipse.jgit.diff.DiffEntry.ChangeType._
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathFilterGroup

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.util.control.Breaks._

class Validator(owner: String, name: String, branch: String, dir: File, metrics: List[Metric]) {
  val repo = new Repo(owner, name, branch, dir)

  def run(): Unit = {
    val compiler = new Compiler
    val unitsToFaults = mutable.Map[String, mutable.Map[String, Int]]() // Map from filename to map of qual names and faults
    val metricRunner = new MetricRunner(List())
    val faultyCommits = repo.getFaultyCommits
    faultyCommits.foreachEntry((commit, faults) => breakable {
      val diffs = repo.diff(commit.getParent(0).getTree, commit.getTree)
      if (diffs.isEmpty) break
      val diffMap = diffs.map(diff => diff.getNewPath -> diff).toMap

      val walk = new TreeWalk(repo.git.getRepository)
      walk.addTree(commit.getTree)
      walk.setRecursive(true)
      // FIXME: Load all files of this version to prevent issues
      walk.setFilter(PathFilterGroup.createFromStrings(diffMap.keySet.asJava))

      while (walk.next()) breakable {
        val path = walk.getPathString
        val diff = diffMap(path)
        diff.getChangeType match {
          case ADD => break // Added file, did not contain the bug
          case MODIFY => // Continue as usual
          case DELETE => break // Can't analyse deleted file
          case RENAME => if (unitsToFaults.contains(diff.getOldPath)) // If the map contains a result for the renamed file
          unitsToFaults(path) = unitsToFaults.remove(diff.getOldPath).get // Move it to the new filename
          case COPY => if (unitsToFaults.contains(diff.getOldPath)) // If the map contains a result for the copied file
          unitsToFaults(path) = mutable.Map.from(unitsToFaults(diff.getOldPath)) // Copy it to the new file
        }
        if (!unitsToFaults.contains(path)) {
          unitsToFaults(path) = mutable.Map("" -> faults) // This file itself contains faults
        } else {
          unitsToFaults(path)("") += faults // This file itself contains faults
        }
        val fileFaults = unitsToFaults(path)

        def addResult(prefix: String, result: Result): Unit = {
          val name = if (!prefix.isEmpty) prefix + " - " + result.name else result.name
          // TODO: Check diff for changes
          if (fileFaults.contains(name))
            fileFaults(name) += faults
          else
            fileFaults(name) = faults
          result.results.foreach(child => addResult(name, child))
        }

        val objectId = walk.getObjectId(0)
        val contents = new String(repo.git.getRepository.open(objectId).getCachedBytes, StandardCharsets.UTF_8)
        val source = Compiler.stringToSource(path, contents)
        val tree = compiler.treeFromSource(source)
        // FIXME: Is race condition caused by casting to global trees?
        metricRunner.run(tree) match {
          case Some(resultTree) => resultTree.results.foreach(result => addResult("", result))
          case None => println("No metric result", tree)
        }
      }
      // Calculate changed files
      // Check if function or object contains changes for the file
    })
    // Analyse repo and add counted faults for each function or object
    println(unitsToFaults)
  }
}
