package validator

import java.io.File

import codeAnalysis.metrics.baseline._

class BaselineValidatorTest extends UnitSpec {
  test("Akka test") {
    val validator = new Validator(
      "akka",
      "akka",
      "master",
      new File("data/projects/akka"),
      new File("data/metricResults/baseline/akka"),
      List("bug"),
      List(
        CouplingBetweenObjects,
        CyclomaticComplexity,
        DepthOfInheritance,
        LackOfCohesionInMethods,
        LinesOfCode,
        NumberOfChildren,
        OutDegree,
        PatternSize,
        ResponseForClass,
        WeightedMethodCount
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
      new File("data/metricResults/baseline/gitbucket"),
      List("bug"),
      List(
        CouplingBetweenObjects,
        CyclomaticComplexity,
        DepthOfInheritance,
        LackOfCohesionInMethods,
        LinesOfCode,
        NumberOfChildren,
        OutDegree,
        PatternSize,
        ResponseForClass,
        WeightedMethodCount
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
      new File("data/metricResults/baseline/http4s"),
      List("bug"),
      List(
        CouplingBetweenObjects,
        CyclomaticComplexity,
        DepthOfInheritance,
        LackOfCohesionInMethods,
        LinesOfCode,
        NumberOfChildren,
        OutDegree,
        PatternSize,
        ResponseForClass,
        WeightedMethodCount
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
      new File("data/metricResults/baseline/quill"),
      List("bug"),
      List(
        CouplingBetweenObjects,
        CyclomaticComplexity,
        DepthOfInheritance,
        LackOfCohesionInMethods,
        LinesOfCode,
        NumberOfChildren,
        OutDegree,
        PatternSize,
        ResponseForClass,
        WeightedMethodCount
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
      new File("data/metricResults/baseline/scio"),
      List("bug \uD83D\uDC1E"),
      List(
        CouplingBetweenObjects,
        CyclomaticComplexity,
        DepthOfInheritance,
        LackOfCohesionInMethods,
        LinesOfCode,
        NumberOfChildren,
        OutDegree,
        PatternSize,
        ResponseForClass,
        WeightedMethodCount
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
      new File("data/metricResults/baseline/shapeless"),
      List("Bug"),
      List(
        CouplingBetweenObjects,
        CyclomaticComplexity,
        DepthOfInheritance,
        LackOfCohesionInMethods,
        LinesOfCode,
        NumberOfChildren,
        OutDegree,
        PatternSize,
        ResponseForClass,
        WeightedMethodCount
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
      new File("data/metricResults/baseline/zio"),
      List("bug"),
      List(
        CouplingBetweenObjects,
        CyclomaticComplexity,
        DepthOfInheritance,
        LackOfCohesionInMethods,
        LinesOfCode,
        NumberOfChildren,
        OutDegree,
        PatternSize,
        ResponseForClass,
        WeightedMethodCount
      )
    )
    validator.run()
  }
}
