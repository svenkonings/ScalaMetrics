package codeAnalysis.analyser

import codeAnalysis.UnitSpec
import codeAnalysis.analyser.metric.{FileResult, MethodResult, MetricResult, ObjectResult}

class ResultTest extends UnitSpec {
  test("Result metrics test") {
    val result = FileResult("", "", 0 ,0)
    result.addResults(List(
      FileResult("", "", 0 ,0) addMetric MetricResult("A", 1),
      ObjectResult("", "", 0 ,0) addMetric MetricResult("E", 5),
      MethodResult("", "", 0 ,0) addMetric MetricResult("F", 6)
    )).addMetrics(List(
      MetricResult("B", 2),
      MetricResult("C", 3),
      MetricResult("D", 4)
    ))
    val metrics = result.metrics
    assert(metrics.size == 3)

    val allMetrics = result.allMetrics
    assert(allMetrics.size == 6)
  }
}
