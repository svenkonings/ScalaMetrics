package codeAnalysis.metrics.multiparadigm.constructs

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{Metric, MetricProducer, MetricResult, ObjectMetric}

object NumberOfReturns extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new NumberOfReturns(compiler)
}

class NumberOfReturns(override val compiler: Compiler) extends ObjectMetric {

  import global.TreeExtensions

  def numberOfReturns(tree: global.Tree): Int = tree.countTraverse {
    case _: global.Return => true
  }

  override def run(tree: global.ImplDef): List[MetricResult] = List(
    MetricResult("NumberOfReturns", numberOfReturns(tree))
  )
}
