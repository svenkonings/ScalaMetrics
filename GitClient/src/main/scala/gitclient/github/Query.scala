package gitclient.github

import ujson.Obj

import scala.io.Source

object Query {
  lazy val token: String = {
    var source: Source = null
    try {
      source = Source.fromFile(".github")
      source.getLines().find(_.startsWith("oauth=")).get.substring(6)
    }
    finally {
      if (source != null) source.close()
    }
  }

  def queryAllPullRequestsAndIssues(owner: String, name: String): Obj = {
    val pullRequests = ujson.Arr()
    val issues = ujson.Arr()

    var withPull = true
    var withIssue = true
    var afterPull: String = null
    var afterIssue: String = null

    while (withPull || withIssue) {
      val query = Query.queryPullRequestsAndIssues(owner, name, withPull, withIssue, afterPull, afterIssue)
      if (withPull) {
        val pullResult = query("pullRequests").obj
        val pullPageInfo = pullResult("pageInfo").obj
        withPull = pullPageInfo("hasNextPage").bool
        afterPull = pullPageInfo("endCursor").strOpt.orNull
        pullRequests.value ++= pullResult("nodes").arr
      }
      if (withIssue) {
        val issueResult = query("issues").obj
        val issuePageInfo = issueResult("pageInfo").obj
        withIssue = issuePageInfo("hasNextPage").bool
        afterIssue = issuePageInfo("endCursor").strOpt.orNull
        issues.value ++= issueResult("nodes").arr
      }
    }

    ujson.Obj(
      "pullRequests" -> pullRequests,
      "issues" -> issues
    )
  }

  def queryPullRequestsAndIssues(owner: String, name: String, withPull: Boolean, withIssue: Boolean, afterPull: String = null, afterIssue: String = null): Obj = {
    println(s"Querying pull requests and issues (owner: $owner, name: $name, afterPull: $afterPull, afterIssue: $afterIssue)")
    val query =
      """
        |query PullRequestsAndIssues($owner: String!, $name: String!, $withPull: Boolean!, $afterPull: String, $withIssue: Boolean!, $afterIssue: String){
        |  repository(owner: $owner, name: $name) {
        |    pullRequests(states: MERGED, first: 100, after: $afterPull) @include(if: $withPull) {
        |      pageInfo {
        |        hasNextPage
        |        endCursor
        |      }
        |      nodes {
        |        number
        |        title
        |        bodyText
        |      }
        |    }
        |    issues(states: CLOSED, labels: "bug", first: 100, after: $afterIssue) @include(if: $withIssue) {
        |      pageInfo {
        |        hasNextPage
        |        endCursor
        |      }
        |      nodes {
        |        number
        |      }
        |    }
        |  }
        |}
       """.stripMargin
    val variables = ujson.Obj(
      "owner" -> owner,
      "name" -> name,
      "withPull" -> withPull,
      "withIssue" -> withIssue
    )
    if (afterPull != null) variables.value += "afterPull" -> afterPull
    if (afterIssue != null) variables.value += "afterIssue" -> afterIssue
    executeQuery(query, variables)
  }

  def executeQuery(query: String, variables: Obj): Obj = {
    val headers = Map("Authorization" -> s"Bearer $token")
    val data = ujson.Obj(
      "query" -> query,
      "variables" -> variables
    )
    val r = requests.post("https://api.github.com/graphql", headers = headers, data = data)
    ujson.read(r.text).obj("data").obj("repository").obj
  }
}
