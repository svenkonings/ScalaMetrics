package codeAnalysis.metrics.multiparadigm.constructs

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{Metric, MetricProducer, MetricResult, ObjectMetric}

object UsageOfNull extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new UsageOfNull(compiler)
}

class UsageOfNull(override val compiler: Compiler) extends ObjectMetric {

  import global.TreeExtensions

  def usageOfNull(tree: global.Tree): Int = tree.countTraverse {
    case tree: global.Literal => tree.value.value == null
  }

  override def run(tree: global.ImplDef): List[MetricResult] = List(
    MetricResult("UsageOfNull", usageOfNull(tree))
  )
}
