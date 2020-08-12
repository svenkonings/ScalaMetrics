package validator

import java.io.File

import codeAnalysis.metrics.paradigmScore._

class ParadigmScoreValidatorTest extends UnitSpec {
  test("Akka test") {
    val validator = new Validator(
      "akka",
      "akka",
      "master",
      new File("data/projects/akka"),
      new File("data/metricResults/paradigmScore/akka"),
      List("bug"),
      List(
        ParadigmScoreBool,
        ParadigmScoreCount,
        ParadigmScoreFraction,
        ParadigmScoreLandkroon
      )
    )
    validator.run()
  }

  test("Gitbucket test") {
    val validator = new Validator(
      "gitbucket",
      "gitbucket",
      "master",
      new File("data/projects/gitbucket"),
      new File("data/metricResults/paradigmScore/gitbucket"),
      List("bug"),
      List(
        ParadigmScoreBool,
        ParadigmScoreCount,
        ParadigmScoreFraction,
        ParadigmScoreLandkroon
      )
    )
    validator.run()
  }

  test("Http4s test") {
    val validator = new Validator(
      "http4s",
      "http4s",
      "master",
      new File("data/projects/http4s"),
      new File("data/metricResults/paradigmScore/http4s"),
      List("bug"),
      List(
        ParadigmScoreBool,
        ParadigmScoreCount,
        ParadigmScoreFraction,
        ParadigmScoreLandkroon
      )
    )
    validator.run()
  }

  test("Quill test") {
    val validator = new Validator(
      "getquill",
      "quill",
      "master",
      new File("data/projects/quill"),
      new File("data/metricResults/paradigmScore/quill"),
      List("bug"),
      List(
        ParadigmScoreBool,
        ParadigmScoreCount,
        ParadigmScoreFraction,
        ParadigmScoreLandkroon
      )
    )
    validator.run()
  }

  test("Scio test") {
    val validator = new Validator(
      "spotify",
      "scio",
      "master",
      new File("data/projects/scio"),
      new File("data/metricResults/paradigmScore/scio"),
      List("bug \uD83D\uDC1E"),
      List(
        ParadigmScoreBool,
        ParadigmScoreCount,
        ParadigmScoreFraction,
        ParadigmScoreLandkroon
      )
    )
    validator.run()
  }

  test("Shapeless test") {
    val validator = new Validator(
      "milessabin",
      "shapeless",
      "master",
      new File("data/projects/shapeless"),
      new File("data/metricResults/paradigmScore/shapeless"),
      List("Bug"),
      List(
        ParadigmScoreBool,
        ParadigmScoreCount,
        ParadigmScoreFraction,
        ParadigmScoreLandkroon
      )
    )
    validator.run()
  }

  test("ZIO test") {
    val validator = new Validator(
      "zio",
      "zio",
      "master",
      new File("data/projects/zio"),
      new File("data/metricResults/paradigmScore/zio"),
      List("bug"),
      List(
        ParadigmScoreBool,
        ParadigmScoreCount,
        ParadigmScoreFraction,
        ParadigmScoreLandkroon
      )
    )
    validator.run()
  }
}
