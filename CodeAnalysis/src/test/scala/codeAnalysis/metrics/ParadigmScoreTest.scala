package codeAnalysis.metrics

import codeAnalysis.UnitSpec
import codeAnalysis.analyser.Analyser

class ParadigmScoreTest extends UnitSpec {
  test("Paradigm Score test") {
    val metrics = new Analyser(resources + "metrics", List(ParadigmScore), true).analyse()
    metrics.foreach(println)
  }
}
