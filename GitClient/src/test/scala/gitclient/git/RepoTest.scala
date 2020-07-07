package gitclient.git

import java.io.File

import gitclient.UnitSpec
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.util.io.DisabledOutputStream

class RepoTest extends UnitSpec {
  test("Log test") {
    val repo = new Repo("gitbucket", "gitbucket", "master", new File("../gitbucketTest"))
    val commits = repo.getFaultyCommits
    println(commits.size)
  }

  test("Diff test") {
    val repo = new Repo("gitbucket", "gitbucket", "master", new File("../gitbucketTest"))
    val commits = repo.getFaultyCommits
    val commit = commits.keySet.iterator.next()
    val diff = repo.diff(commit.getParent(0).getTree.getId, commit.getTree.getId)
    val fileheader = new DiffFormatter(DisabledOutputStream.INSTANCE)
    fileheader.setRepository(repo.git.getRepository)
    val filediff = fileheader.toFileHeader(diff.get(0))
    assert(!diff.isEmpty)
  }
}
