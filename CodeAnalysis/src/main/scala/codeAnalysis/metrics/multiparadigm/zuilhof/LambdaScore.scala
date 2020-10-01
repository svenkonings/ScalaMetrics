package codeAnalysis.metrics.multiparadigm.zuilhof

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{Metric, MetricProducer, MetricResult, ObjectMetric}
import codeAnalysis.util.Extensions.DoubleExtension

object LambdaScore extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new LambdaScore(compiler)
}

class LambdaScore(override val compiler: Compiler) extends ObjectMetric {

  import global.TreeExtensions

  def lambdaScore(tree: global.ImplDef): Double = {
    val lambdaLines = tree.linesTraverse {
      case _: global.Function => true
    }
    val sourceLines = tree.linesTraverse(_ => true)
    lambdaLines \ sourceLines
  }

  override def run(tree: global.ImplDef): List[MetricResult] = List(
    MetricResult("LambdaScore", lambdaScore(tree))
  )
}
