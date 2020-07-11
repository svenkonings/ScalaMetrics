package gitclient.git

import java.io.File

import gitclient.github.Github
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.patch.FileHeader
import org.eclipse.jgit.revwalk.filter.MessageRevFilter
import org.eclipse.jgit.revwalk.{RevCommit, RevTree}
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter
import org.eclipse.jgit.util.io.DisabledOutputStream

import scala.collection.immutable.SortedMap
import scala.collection.mutable
import scala.jdk.CollectionConverters._

class Repo(owner: String, name: String, branch: String, dir: File) {
  lazy val git: Git = Git.open(dir)
  lazy val faults: Map[Int, Int] = Github.getFaults(owner, name)
  lazy val formatter: DiffFormatter = {
    val result = new DiffFormatter(DisabledOutputStream.INSTANCE)
    result.setRepository(git.getRepository)
    result.setPathFilter(PathSuffixFilter.create(".scala"))
    result
  }

  if (!dir.exists()) cloneRepo()

  def cloneRepo(): Unit = Git.cloneRepository()
    .setURI(Github.repoToUri(owner, name))
    .setBranch(branch)
    .setDirectory(dir)
    .call()

  def getFaultyCommits: SortedMap[RevCommit, Int] = SortedMap.from(
    git.log()
      .setRevFilter(MessageRevFilter.create("""#\d+"""))
      .call()
      .asScala
      .map(commit => commit -> countFaults(commit.getFullMessage))
      .filter(_._2 > 0)
  )(Ordering.by(_.getCommitTime))

  def countFaults(message: String): Int =
    Github.findIssues(message).map(issue => faults.getOrElse(issue, 0)).sum

  def diff(oldTree: RevTree, newTree: RevTree): mutable.Buffer[FileHeader] =
    formatter.scan(oldTree, newTree).asScala.map(formatter.toFileHeader)
}
