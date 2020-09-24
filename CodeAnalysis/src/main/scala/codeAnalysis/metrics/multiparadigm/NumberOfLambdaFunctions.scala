package codeAnalysis.metrics.multiparadigm

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{Metric, MetricProducer, MetricResult, ObjectMetric}

object NumberOfLambdaFunctions extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new NumberOfLambdaFunctions(compiler)
}

class NumberOfLambdaFunctions(override val compiler: Compiler) extends ObjectMetric {

  import global.TreeExtensions

  def numberOfLambdaFunctions(tree: global.ImplDef): Int = tree.count {
    case _: global.Function => true
  }

  override def run(tree: global.ImplDef): List[MetricResult] = List(
    MetricResult("NumberOfLambdaFunctions", numberOfLambdaFunctions(tree))
  )
}
