package gitclient.github

import gitclient.UnitSpec

class GithubTest extends UnitSpec{
  test("Get pulls and issues test") {
    val response = Github.getPullRequestsAndFaults("akka", "akka")
    println(response)
  }
}
