package gitclient.git

import java.io.File

import gitclient.github.Github
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.patch.FileHeader
import org.eclipse.jgit.revwalk.filter.MessageRevFilter
import org.eclipse.jgit.revwalk.{RevCommit, RevTree, RevWalk}
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter
import org.eclipse.jgit.util.io.DisabledOutputStream

import scala.collection.immutable.SortedMap
import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.util.Using.resource

class Repo(owner: String, name: String, branch: String, dir: File) extends AutoCloseable {
  val git: Git = if (dir.exists()) Git.open(dir) else cloneRepo()
  val faults: Map[Int, Int] = Github.getFaults(owner, name)

  def cloneRepo(): Git = Git.cloneRepository()
    .setURI(Github.repoToUri(owner, name))
    .setBranch(branch)
    .setDirectory(dir)
    .call()

  def getHeadCommit: RevCommit =
    resource(new RevWalk(git.getRepository)) { revWalk =>
      revWalk.parseCommit(git.getRepository.resolve(Constants.HEAD))
    }

  def getFaultyCommits: SortedMap[RevCommit, Int] = {
    val commits = resource(
      git.log()
        .setRevFilter(MessageRevFilter.create("""#\d+"""))
        .call()
        .asInstanceOf[RevWalk]
    ) { revWalk =>
      revWalk.asScala
        .map(commit => commit -> countFaults(commit.getFullMessage))
        .filter(_._2 > 0) // Commit has more than 0 faults
    }
    val ordering = Ordering.by((commit: RevCommit) => commit.getCommitTime).reverse
    SortedMap.from(commits)(ordering)
  }

  def countFaults(message: String): Int =
    Github.findReferences(message).map(issue => faults.getOrElse(issue, 0)).sum

  def diff(oldTree: RevTree, newTree: RevTree): mutable.Buffer[FileHeader] =
    resource(new DiffFormatter(DisabledOutputStream.INSTANCE)) { formatter =>
      formatter.setRepository(git.getRepository)
      formatter.setPathFilter(PathSuffixFilter.create(".scala"))
      formatter.scan(oldTree, newTree).asScala.map(formatter.toFileHeader)
    }

  override def close(): Unit = git.close()
}
