package gitclient.github

import ujson.Value.Value

class PullRequest(owner: String, name: String, json: Map[String, Value]) {
  val number: Int = json("number").num.toInt
  val title: String = json("title").str
  val body: String = json("bodyText").str
  lazy val commits: List[Commit] = Github.processCommitResult(owner, name, number, json("commits").obj)
}
