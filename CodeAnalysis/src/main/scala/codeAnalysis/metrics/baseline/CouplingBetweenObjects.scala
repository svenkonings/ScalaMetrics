package codeAnalysis.metrics.baseline

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{Metric, MetricProducer, MetricResult, ObjectMetric}

object CouplingBetweenObjects extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new CouplingBetweenObjects(compiler)
}

class CouplingBetweenObjects(override val compiler: Compiler) extends ObjectMetric {

  import global.TreeExtensions

  def couplingBetweenObjects(tree: global.ImplDef): Int = tree.myCollect {
    case tree if tree.getTypeSymbol != null => tree.getTypeSymbol
  }.toSet.size

  override def run(tree: global.ImplDef): List[MetricResult] = List(
    MetricResult("CouplingBetweenObjects", couplingBetweenObjects(tree))
  )
}
