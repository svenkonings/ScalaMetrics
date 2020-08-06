package codeAnalysis.metrics

import codeAnalysis.UnitSpec
import codeAnalysis.analyser.Analyser
import codeAnalysis.analyser.metric.MetricProducer

class MetricTest extends UnitSpec {
  def print(producer: MetricProducer): Unit = {
    val metrics = new Analyser(resources + "metrics", List(producer), true).analyse()
    metrics.foreach(println)
  }

  test("Paradigm Score Bool test") {
    print(ParadigmScoreBool)
  }

  test("Paradigm Score Count test") {
    print(ParadigmScoreCount)
  }

  test("Paradigm Score Fraction test") {
    print(ParadigmScoreFraction)
  }

  test("Paradigm Score Landkroon test") {
    print(ParadigmScoreLandkroon)
  }

  test("Source Lines of Code test") {
    print(SourceLinesOfCode)
  }
}
