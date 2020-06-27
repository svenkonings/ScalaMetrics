package gitclient.github

import ujson.Value.Value

import scala.collection.mutable

object Github {
  def getPullRequestsAndFaults(owner: String, name: String): PullRequestsAndFaults = {
    val pullRequests = mutable.ListBuffer[PullRequest]()
    val faults = mutable.Set[Int]()

    var withPull = true
    var withIssue = true
    var afterPull: String = null
    var afterIssue: String = null
    while (withPull || withIssue) {
      val query = Query.queryPullRequestsAndIssues(owner, name, withPull, withIssue, afterPull, afterIssue)
      val repository = query.obj("data").obj("repository").obj
      if (withPull) {
        val pullResult = repository("pullRequests").obj
        val pullPageInfo = pullResult("pageInfo").obj
        withPull = pullPageInfo("hasNextPage").bool
        afterPull = pullPageInfo("endCursor").strOpt.orNull
        pullRequests ++= pullResult("nodes").arr.map(node => new PullRequest(owner, name, node.obj.toMap))
      }
      if (withIssue) {
        val issueResult = repository("issues").obj
        val issuePageInfo = issueResult("pageInfo").obj
        withIssue = issuePageInfo("hasNextPage").bool
        afterIssue = issuePageInfo("endCursor").strOpt.orNull
        // Assumes issues representing faults are labeled with "bug" (see query)
        faults ++= issueResult("nodes").arr.map(_.obj("number").num.toInt)
      }
    }
    PullRequestsAndFaults(pullRequests.toList, faults.toSet)
  }

  def processCommitResult(owner: String, name: String, number: Int, commitResult: mutable.LinkedHashMap[String, Value]): List[Commit] = {
    def nodesToCommits(nodes: Value): mutable.ArrayBuffer[Commit] = nodes.arr.map(node => {
      val commit = node.obj("commit").obj
      Commit(commit("oid").str, commit("message").str)
    })

    val commits = nodesToCommits(commitResult("nodes"))

    val pageInfo = commitResult("pageInfo").obj
    var hasNextPage = pageInfo("hasNextPage").bool
    var afterCommit = pageInfo("endCursor").strOpt.orNull

    while (hasNextPage) {
      val query = Query.queryPullRequestCommits(owner, name, number, afterCommit)
      val commitResult = query.obj("data").obj("repository").obj("pullRequest").obj("commits").obj
      val pageInfo = commitResult("pageInfo").obj
      hasNextPage = pageInfo("hasNextPage").bool
      afterCommit = pageInfo("endCursor").strOpt.orNull
      commits ++= nodesToCommits(commitResult("nodes"))
    }

    commits.toList
  }
}
