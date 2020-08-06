package validator

import java.io.File

import codeAnalysis.metrics.{ParadigmScoreBool, ParadigmScoreLandkroon, ParadigmScoreCount, ParadigmScoreFraction}

class ValidatorTest extends UnitSpec {
  test("Gitbucket test") {
    val validator = new Validator("gitbucket", "gitbucket", "master", new File("target/gitbucket"), List(ParadigmScoreBool, ParadigmScoreCount, ParadigmScoreFraction, ParadigmScoreLandkroon))
    validator.run()
  }

  test("Akka test") {
    val validator = new Validator("akka", "akka", "master", new File("target/akka"), List(ParadigmScoreBool, ParadigmScoreCount, ParadigmScoreFraction, ParadigmScoreLandkroon))
    validator.run()
  }
}
