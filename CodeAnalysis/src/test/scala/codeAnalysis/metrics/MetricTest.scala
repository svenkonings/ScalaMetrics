package codeAnalysis.metrics

import codeAnalysis.UnitSpec
import codeAnalysis.analyser.Analyser
import codeAnalysis.analyser.metric.MetricProducer
import codeAnalysis.metrics.baseline._
import codeAnalysis.metrics.paradigmScore._

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

  test("Lines of Code test") {
    print(LinesOfCode)
  }

  test("Depth of Inheritance test") {
    print(DepthOfInheritance)
  }

  test("Number of Children test") {
    print(NumberOfChildren)
  }

  test("Lack Of Cohesion In Methods test") {
    print(LackOfCohesionInMethods)
  }

  test("Response for Class test") {
    print(ResponseForClass)
  }

  test("Cyclomatic Complexity test") {
    print(CyclomaticComplexity)
  }

  test("Coupling Between Objects test") {
    print(CouplingBetweenObjects)
  }
}
