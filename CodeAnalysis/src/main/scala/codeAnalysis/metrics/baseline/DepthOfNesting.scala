package codeAnalysis.metrics.baseline

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{MethodMetric, Metric, MetricProducer, MetricResult}

object DepthOfNesting extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new DepthOfNesting(compiler)
}

class DepthOfNesting(override val compiler: Compiler) extends MethodMetric {

  import global.TreeExtensions

  def depthOfNesting(tree: global.DefDef): Int = tree.sum {
    case tree: global.CaseDef => tree.body.count(_ => true)
  }

  override def run(tree: global.DefDef): List[MetricResult] = List(
    MetricResult("DepthOfNesting", depthOfNesting(tree))
  )
}
