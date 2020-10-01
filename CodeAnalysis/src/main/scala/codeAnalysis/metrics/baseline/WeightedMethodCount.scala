package codeAnalysis.metrics.baseline

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{Metric, MetricProducer, MetricResult, ObjectMetric}

object WeightedMethodCount extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new WeightedMethodCount(compiler)
}

class WeightedMethodCount(override val compiler: Compiler) extends ObjectMetric {

  import global.TreeExtensions

  def weightedMethodCount(tree: global.ImplDef): Int =
    tree.collectTraverse { case defDef: global.DefDef if defDef.symbol.owner == tree.symbol => defDef }
      .map(_.cyclomaticComplexity)
      .sum

  override def run(tree: global.ImplDef): List[MetricResult] = List(
    MetricResult("WeightedMethodCount", weightedMethodCount(tree))
  )
}
