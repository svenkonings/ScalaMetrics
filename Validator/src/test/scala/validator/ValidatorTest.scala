package validator

import java.io.File

import codeAnalysis.metrics.baseline.DepthOfInheritance
import codeAnalysis.metrics.paradigmScore._

class ValidatorTest extends UnitSpec {
  test("Scala-JS test") {
    val validator = new Validator("scala-js", "scala-js", "master", new File("target/scala-js"), List("bug"), List(ParadigmScoreBool, ParadigmScoreCount, ParadigmScoreFraction, ParadigmScoreLandkroon))
    validator.run()
  }

  test("Akka test") {
    val validator = new Validator("akka", "akka", "master", new File("target/akka"), List("bug"), List(ParadigmScoreBool, ParadigmScoreCount, ParadigmScoreFraction, ParadigmScoreLandkroon))
    validator.run()
  }

  test("Scio test") {
    val validator = new Validator("spotify", "scio", "master", new File("target/scio"), List("bug \uD83D\uDC1E"), List(ParadigmScoreBool, ParadigmScoreCount, ParadigmScoreFraction, ParadigmScoreLandkroon))
    validator.run()
  }

  test("Gitbucket test") {
    val validator = new Validator("gitbucket", "gitbucket", "master", new File("target/gitbucket"), List("bug"), List(ParadigmScoreBool, ParadigmScoreCount, ParadigmScoreFraction, ParadigmScoreLandkroon))
    validator.run()
  }

  test("Baseline Gitbucket test") {
    val validator = new Validator("gitbucket", "gitbucket", "master", new File("target/gitbucket"), List("bug"), List(DepthOfInheritance))
    validator.run()
  }

  test("Quill test") {
    val validator = new Validator("getquill", "quill", "master", new File("target/quill"), List("bug"), List(ParadigmScoreBool, ParadigmScoreCount, ParadigmScoreFraction, ParadigmScoreLandkroon))
    validator.run()
  }

  test("Http4s test") {
    val validator = new Validator("http4s", "http4s", "master", new File("target/http4s"), List("bug"), List(ParadigmScoreBool, ParadigmScoreCount, ParadigmScoreFraction, ParadigmScoreLandkroon))
    validator.run()
  }

  test("ZIO test") {
    val validator = new Validator("zio", "zio", "master", new File("target/zio"), List("bug"), List(ParadigmScoreBool, ParadigmScoreCount, ParadigmScoreFraction, ParadigmScoreLandkroon))
    validator.run()
  }

  test("Scalafmt test") {
    val validator = new Validator("scalameta", "scalafmt", "master", new File("target/scalafmt"), List("bug"), List(ParadigmScoreBool, ParadigmScoreCount, ParadigmScoreFraction, ParadigmScoreLandkroon))
    validator.run()
  }

  test("Coursier test") {
    val validator = new Validator("coursier", "coursier", "master", new File("target/coursier"), List("bug"), List(ParadigmScoreBool, ParadigmScoreCount, ParadigmScoreFraction, ParadigmScoreLandkroon))
    validator.run()
  }

  test("Slick test") {
    val validator = new Validator("slick", "slick", "master", new File("target/slick"), List("bug"), List(ParadigmScoreBool, ParadigmScoreCount, ParadigmScoreFraction, ParadigmScoreLandkroon))
    validator.run()
  }

  test("Lagom test") {
    val validator = new Validator("lagom", "lagom", "master", new File("target/lagom"), List("type:defect"), List(ParadigmScoreBool, ParadigmScoreCount, ParadigmScoreFraction, ParadigmScoreLandkroon))
    validator.run()
  }

  test("Shapeless test") {
    val validator = new Validator("milessabin", "shapeless", "master", new File("target/shapeless"), List("Bug"), List(ParadigmScoreBool, ParadigmScoreCount, ParadigmScoreFraction, ParadigmScoreLandkroon))
    validator.run()
  }
}
