package validator

import java.io.File

import codeAnalysis.metrics.{ParadigmScoreBool, ParadigmScoreClassic, ParadigmScoreCount, ParadigmScoreFraction}
class ValidatorTest extends UnitSpec {
  test("Gitbucket Briand test") {
    val validator = new ValidatorBriand("gitbucket", "gitbucket", "master", new File("target/gitbucket"), List(ParadigmScoreBool, ParadigmScoreCount, ParadigmScoreFraction, ParadigmScoreClassic))
    validator.run()
  }

  test("Akka Briand test") {
    val validator = new ValidatorBriand("akka", "akka", "master", new File("target/akka"), List(ParadigmScoreBool, ParadigmScoreCount, ParadigmScoreFraction, ParadigmScoreClassic))
    validator.run()
  }

  test("Gitbucket Landkroon test") {
    val validator = new ValidatorLandkroon2("gitbucket", "gitbucket", "master", new File("target/gitbucket"), List(ParadigmScoreBool, ParadigmScoreCount, ParadigmScoreFraction, ParadigmScoreClassic))
    validator.run()
  }

  test("Akka Landkroon test") {
    val validator = new ValidatorLandkroon2("akka", "akka", "master", new File("target/akka"), List(ParadigmScoreBool, ParadigmScoreCount, ParadigmScoreFraction, ParadigmScoreClassic))
    validator.run()
  }
}
