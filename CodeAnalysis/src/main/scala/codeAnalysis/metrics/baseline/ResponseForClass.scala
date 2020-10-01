package codeAnalysis.metrics.baseline

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{Metric, MetricProducer, MetricResult, ObjectMetric}

object ResponseForClass extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new ResponseForClass(compiler)
}

class ResponseForClass(override val compiler: Compiler) extends ObjectMetric {

  import global.TreeExtensions

  def responseForClass(tree: global.ImplDef): Int = tree.collectTraverse {
    case defDef: global.DefDef => defDef.symbol
    case select: global.Select if select.isMethod => select.symbol
  }.toSet.size

  override def run(tree: global.ImplDef): List[MetricResult] =
    List(MetricResult("ResponseForClass", responseForClass(tree)))
}
