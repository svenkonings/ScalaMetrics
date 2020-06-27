package codeAnalysis.analyser

import codeAnalysis.UnitSpec
import codeAnalysis.analyser.metric.{FileResult, MethodResult, MetricResult, ObjectResult}

class ResultTest extends UnitSpec {
  test("Result metrics test") {
    val result = FileResult(null)
    result.addResults(List(
      FileResult(null) addMetric MetricResult("A", 1),
      ObjectResult(null) addMetric MetricResult("E", 5),
      MethodResult(null) addMetric MetricResult("F", 6)
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
