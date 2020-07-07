package gitclient.git

import java.io.File
import java.util

import gitclient.github.Github
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.filter.MessageRevFilter
import org.eclipse.jgit.treewalk.CanonicalTreeParser

import scala.jdk.CollectionConverters._

class Repo(owner: String, name: String, branch: String, dir: File) {
  lazy val git: Git = Git.open(dir)
  lazy val faults: Map[Int, Int] = Github.getFaults(owner, name)

  def cloneRepo(): Unit = Git.cloneRepository()
    .setURI(Github.repoToUri(owner, name))
    .setBranch(branch)
    .setDirectory(dir)
    .call()

  def getFaultyCommits: Map[RevCommit, Int] = git.log()
    .setRevFilter(MessageRevFilter.create("""#\d+"""))
    .call()
    .asScala
    .map(commit => commit -> countFaults(commit.getFullMessage))
    .filter(_._2 > 0)
    .toMap

  def countFaults(message: String): Int = Github.findIssues(message).map(issue => faults.getOrElse(issue, 0)).sum

  def diff(oldTree: AnyObjectId, newTree: AnyObjectId): util.List[DiffEntry] = {
    val oldTreeIterator = new CanonicalTreeParser(null, git.getRepository.newObjectReader(), oldTree)
    val newTreeIterator = new CanonicalTreeParser(null, git.getRepository.newObjectReader(), newTree)
    git.diff().setOldTree(oldTreeIterator).setNewTree(newTreeIterator).call()
  }
}
