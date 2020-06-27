package gitclient.git

import java.io.File
import java.{lang, util}

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.filter.MessageRevFilter
import org.eclipse.jgit.treewalk.CanonicalTreeParser

class Repo(uri: String, branch: String, dir: File) {
  lazy val git: Git = Git.open(dir)

  def cloneRepo(): Unit = Git.cloneRepository()
    .setURI(uri)
    .setBranch(branch)
    .setDirectory(dir)
    .call()

  def getIssueCommits: lang.Iterable[RevCommit] = git.log()
    .setRevFilter(MessageRevFilter.create("""#\d+"""))
    .call()

  def diff(oldTree: AnyObjectId, newTree: AnyObjectId): util.List[DiffEntry] = {
    val oldTreeIterator = new CanonicalTreeParser(null, git.getRepository.newObjectReader(), oldTree)
    val newTreeIterator = new CanonicalTreeParser(null, git.getRepository.newObjectReader(), newTree)
    git.diff().setOldTree(oldTreeIterator).setNewTree(newTreeIterator).call()
  }
}
