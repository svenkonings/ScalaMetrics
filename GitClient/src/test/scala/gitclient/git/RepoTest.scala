package gitclient.git

import java.io.File

import gitclient.UnitSpec
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.util.io.DisabledOutputStream

class RepoTest extends UnitSpec {
  test("Log test") {
    val repo = new Repo("https://github.com/gitbucket/gitbucket.git", "master", new File("../gitbucketTest"))
    val commits = repo.getIssueCommits
    commits.forEach(commit => println(commit.getFullMessage))
  }

  test("Diff test") {
    val repo = new Repo("https://github.com/gitbucket/gitbucket.git", "master", new File("../gitbucketTest"))
    val commits = repo.getIssueCommits
    val commit = commits.iterator().next()
    val diff = repo.diff(commit.getParent(0).getTree.getId, commit.getTree.getId)
    val fileheader = new DiffFormatter(DisabledOutputStream.INSTANCE)
    fileheader.setRepository(repo.git.getRepository)
    val filediff = fileheader.toFileHeader(diff.get(0))
    assert(!diff.isEmpty)
  }
}
