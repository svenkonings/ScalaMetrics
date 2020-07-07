package gitclient.github

import java.io.FileWriter

import gitclient.UnitSpec

class QueryTest extends UnitSpec {
  test("Query all Akka pull requests and issues") {
    val t1 = System.currentTimeMillis()
    val json = Query.queryAllPullRequestsAndIssues("akka", "akka")
    val t2 = System.currentTimeMillis()
    println(t2 - t1, "msecs")
    var writer: FileWriter = null
    try {
      writer = new FileWriter("target/temp.json")
      ujson.writeTo(json, writer)
    } finally {
      if (writer != null) writer.close()
    }
  }
}
