package validator

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{Metric, MetricProducer, MetricResult}
import codeAnalysis.metrics.baseline._
import codeAnalysis.metrics.paradigmScore.ParadigmScoreFraction

class BaselineValidatorTest extends UnitSpec("baseline", List(
  CouplingBetweenObjects,
  CyclomaticComplexity,
  DepthOfInheritance,
  DepthOfNesting,
  LackOfCohesionInMethods,
  LinesOfCode,
  NumberOfChildren,
  OutDegree,
  PatternSize,
  ResponseForClass,
  WeightedMethodCount,
  BaselineParadigmScore
))

object BaselineParadigmScore extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new BaselineParadigmScore(compiler)
}

class BaselineParadigmScore(override val compiler: Compiler) extends ParadigmScoreFraction(compiler) {
  // Only include last 2 metrics: HasPoints and ParadigmScore
  override def run(tree: global.DefDef): List[MetricResult] = super.run(tree).takeRight(2)
}
