package gitclient.github

import java.io.FileWriter

import gitclient.UnitSpec

import scala.util.Using.resource

class QueryTest extends UnitSpec {
  test("Query all Akka pull requests and issues") {
    val t1 = System.currentTimeMillis()
    val json = Query.queryAllPullRequestsAndIssues("akka", "akka", List("bug"))
    val t2 = System.currentTimeMillis()
    println(t2 - t1, "msecs")
    resource(new FileWriter("target/temp.json"))(writer =>
      ujson.writeTo(json, writer)
    )
  }
}
