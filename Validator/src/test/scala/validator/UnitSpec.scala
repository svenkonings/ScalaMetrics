package validator

import java.io.File

import codeAnalysis.analyser.metric.MetricProducer
import org.scalatest.funsuite.AnyFunSuite

class UnitSpec(folder: String, metrics: List[MetricProducer]) extends AnyFunSuite {
  test("Akka test") {
    val validator = new Validator(
      "akka",
      "akka",
      "master",
      new File("data/projects/akka"),
      new File(s"data/metricResults/$folder/akka"),
      List("bug"),
      metrics
    )
    validator.run()
  }

  test("Gitbucket test") {
    val validator = new Validator(
      "gitbucket",
      "gitbucket",
      "master",
      new File("data/projects/gitbucket"),
      new File(s"data/metricResults/$folder/gitbucket"),
      List("bug"),
      metrics
    )
    validator.run()
  }

  test("Http4s test") {
    val validator = new Validator(
      "http4s",
      "http4s",
      "master",
      new File("data/projects/http4s"),
      new File(s"data/metricResults/$folder/http4s"),
      List("bug"),
      metrics
    )
    validator.run()
  }

  test("Quill test") {
    val validator = new Validator(
      "getquill",
      "quill",
      "master",
      new File("data/projects/quill"),
      new File(s"data/metricResults/$folder/quill"),
      List("bug"),
      metrics
    )
    validator.run()
  }

  test("Scio test") {
    val validator = new Validator(
      "spotify",
      "scio",
      "master",
      new File("data/projects/scio"),
      new File(s"data/metricResults/$folder/scio"),
      List("bug \uD83D\uDC1E"),
      metrics
    )
    validator.run()
  }

  test("Shapeless test") {
    val validator = new Validator(
      "milessabin",
      "shapeless",
      "master",
      new File("data/projects/shapeless"),
      new File(s"data/metricResults/$folder/shapeless"),
      List("Bug"),
      metrics
    )
    validator.run()
  }

  test("ZIO test") {
    val validator = new Validator(
      "zio",
      "zio",
      "master",
      new File("data/projects/zio"),
      new File(s"data/metricResults/$folder/zio"),
      List("bug"),
      metrics
    )
    validator.run()
  }
}
