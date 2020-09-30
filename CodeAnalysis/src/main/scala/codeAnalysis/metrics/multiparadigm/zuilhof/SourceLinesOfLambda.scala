package codeAnalysis.metrics.multiparadigm.zuilhof

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{Metric, MetricProducer, MetricResult, ObjectMetric}

object SourceLinesOfLambda extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new SourceLinesOfLambda(compiler)
}

class SourceLinesOfLambda(override val compiler: Compiler) extends ObjectMetric {

  import global.TreeExtensions

  def sourceLinesOfLambda(tree: global.ImplDef): Int = tree.lines {
    case _: global.Function => true
  }

  override def run(tree: global.ImplDef): List[MetricResult] = List(
    MetricResult("SourceLinesOfLambda", sourceLinesOfLambda(tree))
  )
}
