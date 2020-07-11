package validator

import java.io.File

import codeAnalysis.metrics.ParadigmScore
class ValidatorTest extends UnitSpec {
  test("Gitbucket test") {
    val validator = new Validator("gitbucket", "gitbucket", "master", new File("target/gitbucket"), List(ParadigmScore))
    validator.run()
  }

  test("Akka test") {
    val validator = new Validator("akka", "akka", "master", new File("target/akka"), List(ParadigmScore))
    validator.run()
  }
}
