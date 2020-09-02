package codeAnalysis.metrics.baseline

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{MethodMetric, Metric, MetricProducer, MetricResult}

object OutDegree extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new OutDegree(compiler)
}

class OutDegree(override val compiler: Compiler) extends MethodMetric {

  import global.TreeExtensions

  def outDegree(tree: global.DefDef): Int = tree.count {
    case _: global.Apply => true
  }

  def outDegreeDistinct(tree: global.DefDef): Int = tree.myCollect {
    case tree: global.Apply => tree.fun.symbol
  }.toSet.size

  override def run(tree: global.DefDef): List[MetricResult] = List(
    MetricResult("OutDegree", outDegree(tree)),
    MetricResult("OutDegreeDistinct", outDegreeDistinct(tree))
  )
}
