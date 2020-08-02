package gitclient.github

import gitclient.util.Cache
import ujson.Value.Value

import scala.collection.immutable.HashMap
import scala.util.matching.Regex

object Github {
  val referencePattern: Regex = """#(\d+)""".r

  def findReferences(text: String): Iterator[Int] = referencePattern.findAllIn(text).matchData.map(_.group(1).toInt)

  def repoToUri(owner: String, name: String): String = s"https://github.com/$owner/$name.git"

  /**
   * Get a map of faults:
   * keys - numbers referring to all issues labeled as faults
   * and all pull requests referring to those issues.
   * values - The associated faulty issues.
   */
  def getFaults(owner: String, name: String, useCache: Boolean = true): Map[Int, Set[Int]] =
    if (useCache && Cache.isCached(owner + name)) {
      Cache.readObject(owner + name).asInstanceOf[Map[Int, Set[Int]]]
    } else {
      val pullRequestsAndIssues = Query.queryAllPullRequestsAndIssues(owner, name)
      val pullRequests = pullRequestsAndIssues("pullRequests").arr
      val issues = pullRequestsAndIssues("issues").arr

      def toNumber(pullOrIssue: Value): Int = pullOrIssue.obj("number").num.toInt

      // Faulty issues refer to themselves
      val issueMap = HashMap.from(issues.map(toNumber).map(issue => issue -> Set(issue)))
      val issueNumbers = issueMap.keySet

      def getFaultyIssues(pullRequest: Value): Set[Int] = {
        val pullObject = pullRequest.obj
        val pullText = pullObject("title").str + pullObject("bodyText").str
        val referenceNumbers = findReferences(pullText)
        referenceNumbers.filter(issueNumbers).toSet
      }

      // Faulty pull requests refer to the faulty issues referenced in their description
      val pullRequestMap = HashMap.from(
        pullRequests
          .map(pullRequest => toNumber(pullRequest) -> getFaultyIssues(pullRequest))
          .filter(_._2.nonEmpty) // Only include pull requests that refer to faulty issues
      )

      val faultsMap = issueMap ++ pullRequestMap
      if (useCache) Cache.writeObject(owner + name, faultsMap)
      faultsMap
    }
}
