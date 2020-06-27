package gitclient.github

import ujson.Value.Value

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

  def queryPullRequestsAndIssues(owner: String, name: String, withPull: Boolean, withIssue: Boolean, afterPull: String = null, afterIssue: String = null): Value = {
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
        |        commits(first: 100) {
        |          pageInfo {
        |            hasNextPage
        |            endCursor
        |          }
        |          nodes {
        |            commit {
        |              oid
        |              message
        |            }
        |          }
        |        }
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

  def queryPullRequestCommits(owner: String, name: String, number: Int, afterCommit: String = null): Value = {
    println(s"Querying pull requests commits (owner: $owner, name: $name, number: $number, afterCommit: $afterCommit)")
    val query =
      """
        |query PullRequestCommits($owner: String!, $name: String!, $number: Int!, $afterCommit: String) {
        |  repository(owner: $owner, name: $name) {
        |    pullRequest(number: $number) {
        |      commits(first: 100, after: $afterCommit) {
        |        pageInfo {
        |          hasNextPage
        |          endCursor
        |        }
        |        nodes {
        |          commit {
        |            oid
        |            message
        |          }
        |        }
        |      }
        |    }
        |  }
        |}
      """.stripMargin
    val variables = ujson.Obj(
      "owner" -> owner,
      "name" -> name,
      "number" -> number
    )
    if (afterCommit != null) variables.value += "afterCommit" -> afterCommit
    executeQuery(query, variables)
  }

  def executeQuery(query: String, variables: Value): Value = {
    val headers = Map("Authorization" -> s"Bearer $token")
    val data = ujson.Obj(
      "query" -> query,
      "variables" -> variables
    )
    val r = requests.post("https://api.github.com/graphql", headers = headers, data = data)
    ujson.read(r.text)
  }
}
