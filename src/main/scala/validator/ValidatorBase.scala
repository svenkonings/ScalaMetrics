package validator

import java.io.File
import java.nio.charset.StandardCharsets

import codeAnalysis.analyser.metric.Result
import codeAnalysis.util.FileUtil
import gitclient.git.Repo
import org.eclipse.jgit.diff.DiffEntry.ChangeType._
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.patch.FileHeader
import org.eclipse.jgit.revwalk.RevCommit

import scala.reflect.internal.util.SourceFile

abstract class ValidatorBase(owner: String, name: String, branch: String, dir: File) {
  protected val repo = new Repo(owner, name, branch, dir)

  protected def getDiffs(commit: RevCommit): Map[String, FileHeader] = {
    repo.diff(commit.getParent(0).getTree, commit.getTree)
      .filter(diff =>
        !FileUtil.isTestPath(diff.getNewPath) && // Skip test files
          diff.getChangeType != ADD && // Skip added files (didn't contain the faults)
          diff.getChangeType != DELETE // Skip deleted files (nothing to analyse)
      )
      .map(diff => diff.getNewPath -> diff)
      .toMap
  }

  protected def getContents(objectId: ObjectId): String =
    new String(repo.git.getRepository.open(objectId).getCachedBytes, StandardCharsets.UTF_8)

  protected def getRelativePath(result: Result): String =
    getRelativePath(result.path)

  protected def getRelativePath(source: SourceFile): String =
    getRelativePath(source.file.canonicalPath)

  protected def getRelativePath(path: String): String = path
    .substring(dir.getCanonicalPath.length + 1)
    .replace("\\", "/")
}
