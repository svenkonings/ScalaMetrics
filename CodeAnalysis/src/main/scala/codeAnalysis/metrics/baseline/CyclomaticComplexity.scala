package codeAnalysis.metrics.baseline

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{MethodMetric, Metric, MetricProducer, MetricResult}

object CyclomaticComplexity extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new CyclomaticComplexity(compiler)
}

class CyclomaticComplexity(override val compiler: Compiler) extends MethodMetric {

  import global.TreeExtensions

  override def run(tree: global.DefDef): List[MetricResult] = List(
    MetricResult("CyclomaticComplexity", tree.cyclomaticComplexity)
  )
}
