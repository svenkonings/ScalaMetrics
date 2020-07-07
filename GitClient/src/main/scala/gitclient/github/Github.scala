package gitclient.github

import gitclient.util.Cache
import ujson.Value.Value

import scala.collection.immutable.HashMap
import scala.util.matching.Regex

object Github {
  val issuePattern: Regex = """#(\d+)""".r

  def findIssues(text: String): Iterator[Int] = issuePattern.findAllIn(text).matchData.map(_.group(1).toInt)

  def repoToUri(owner: String, name: String): String = s"https://github.com/$owner/$name.git"

  /**
   * Get a map of faults:
   * keys - numbers referring to all issues labeled as faults
   * and all pull requests referring to those issues.
   * values - The amount of associated issues.
   */
  def getFaults(owner: String, name: String, useCache: Boolean = true): Map[Int, Int] =
    if (useCache && Cache.isCached(owner + name)) {
      Cache.readObject(owner + name).asInstanceOf[Map[Int, Int]]
    } else {
      val pullRequestsAndIssues = Query.queryAllPullRequestsAndIssues(owner, name)
      val pullRequests = pullRequestsAndIssues("pullRequests").arr
      val issues = pullRequestsAndIssues("issues").arr

      def toNumber(pullOrIssue: Value): Int = pullOrIssue.obj("number").num.toInt

      // Faulty issues count as a single fault
      val issueMap = HashMap.from(issues.map(issue => toNumber(issue) -> 1))
      val issueNumbers = issueMap.keySet

      def countIssues(pullRequest: Value): Int = {
        val pullObject = pullRequest.obj
        val pullText = pullObject("title").str + pullObject("bodyText").str
        val numbers = findIssues(pullText)
        numbers.count(issueNumbers)
      }

      // Faults of pull requests are the number of faulty issues they refer to
      val pullRequestMap = HashMap.from(pullRequests
        .map(pullRequest => toNumber(pullRequest) -> countIssues(pullRequest))
        .filter(_._2 > 0)) // Only include pull requests that refer to faulty issues

      val faultsMap = issueMap ++ pullRequestMap
      if (useCache) Cache.writeObject(owner + name, faultsMap)
      faultsMap
    }
}
