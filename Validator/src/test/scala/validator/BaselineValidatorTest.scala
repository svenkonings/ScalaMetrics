package validator

import java.io.File

import codeAnalysis.metrics.baseline.DepthOfInheritance

class BaselineValidatorTest extends UnitSpec {
  test("Gitbucket test") {
    val validator = new Validator(
      "gitbucket",
      "gitbucket",
      "master",
      new File("data/projects/gitbucket"),
      new File("data/metricResults/baseline/gitbucket"),
      List("bug"),
      List(DepthOfInheritance)
    )
    validator.run()
  }
}
