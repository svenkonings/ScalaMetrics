package gitclient.github

import gitclient.UnitSpec

class GithubTest extends UnitSpec {
  test("Get Akka faults") {
    val response = Github.getFaults("akka", "akka", List("bug"))
    println(response)
  }
}
