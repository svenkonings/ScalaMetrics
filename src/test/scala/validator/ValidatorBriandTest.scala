package validator

import java.io.File

import codeAnalysis.metrics.{ParadigmScoreBool, ParadigmScoreClassic, ParadigmScoreCount, ParadigmScoreFraction}
class ValidatorBriandTest extends UnitSpec {
  test("Gitbucket test") {
    val validator = new ValidatorBriand("gitbucket", "gitbucket", "master", new File("target/gitbucket"), List(ParadigmScoreBool, ParadigmScoreCount, ParadigmScoreFraction, ParadigmScoreClassic))
    validator.run()
  }

  test("Akka test") {
    val validator = new ValidatorBriand("akka", "akka", "master", new File("target/akka"), List(ParadigmScoreBool, ParadigmScoreCount, ParadigmScoreFraction, ParadigmScoreClassic))
    validator.run()
  }
}
