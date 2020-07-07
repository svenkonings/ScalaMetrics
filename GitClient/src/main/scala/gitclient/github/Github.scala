package gitclient.github

import gitclient.util.Cache
import ujson.Value.Value

import scala.collection.immutable.HashSet
import scala.util.matching.Regex

object Github {
  val issuePattern: Regex = """#(\d+)""".r

  /**
   * Get a set of numbers referring to all issues labeled as faults
   * and all pull requests referring to those issues.
   */
  def getFaults(owner: String, name: String, useCache: Boolean = true): Set[Int] =
    if (useCache && Cache.isCached(owner + name)) {
      Cache.readObject(owner + name).asInstanceOf[HashSet[Int]]
    } else {
      val pullRequestsAndIssues = Query.queryAllPullRequestsAndIssues(owner, name)
      val pullRequests = pullRequestsAndIssues("pullRequests").arr
      val issues = pullRequestsAndIssues("issues").arr

      def toNumber(pullOrIssue: Value): Int = pullOrIssue.obj("number").num.toInt

      val issueNumbers = HashSet.from(issues.map(toNumber))

      def refersToIssue(pullRequest: Value): Boolean = {
        val pullObject = pullRequest.obj
        val pullText = pullObject("title").str + pullObject("bodyText").str
        val matches = issuePattern.findAllIn(pullText)
        val numbers = matches.matchData.map(_.group(1).toInt).toSet
        numbers.intersect(issueNumbers).nonEmpty
      }

      val pullRequestNumbers = HashSet.from(pullRequests.filter(refersToIssue).map(toNumber))

      val result = issueNumbers.union(pullRequestNumbers)
      if (useCache) Cache.writeObject(owner + name, result)
      result
    }
}
