package codeAnalysis.metrics

import codeAnalysis.UnitSpec
import codeAnalysis.analyser.Analyser

class ParadigmScoreTest extends UnitSpec {
  test("Paradigm Score Bool test") {
    val metrics = new Analyser(resources + "metrics", List(ParadigmScoreBool), true).analyse()
    metrics.foreach(println)
  }

  test("Paradigm Score Count test") {
    val metrics = new Analyser(resources + "metrics", List(ParadigmScoreCount), true).analyse()
    metrics.foreach(println)
  }

  test("Paradigm Score Fraction test") {
    val metrics = new Analyser(resources + "metrics", List(ParadigmScoreFraction), true).analyse()
    metrics.foreach(println)
  }

  test("Paradigm Score Classic test") {
    val metrics = new Analyser(resources + "metrics", List(ParadigmScoreClassic), true).analyse()
    metrics.foreach(println)
  }
}
