package codeAnalysis.metrics.baseline

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{MethodMetric, Metric, MetricProducer, MetricResult}

object DepthOfNesting extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new DepthOfNesting(compiler)
}

class DepthOfNesting(override val compiler: Compiler) extends MethodMetric {

  import global.TreeExtensions

  def depthOfNesting(tree: global.DefDef): Int = {
    def recurisveDepth(tree: global.Tree): Int = 1 + tree.children.map(recurisveDepth).maxOption.getOrElse(0)
    tree.collectTraverse {
      case tree: global.CaseDef => recurisveDepth(tree.body)
    }.maxOption.getOrElse(0)
  }

  override def run(tree: global.DefDef): List[MetricResult] = List(
    MetricResult("DepthOfNesting", depthOfNesting(tree))
  )
}
