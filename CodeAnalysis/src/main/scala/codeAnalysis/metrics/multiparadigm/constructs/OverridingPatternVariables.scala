package codeAnalysis.metrics.multiparadigm.constructs

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{Metric, MetricProducer, MetricResult, ObjectMetric}

object OverridingPatternVariables extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new OverridingPatternVariables(compiler)
}

class OverridingPatternVariables(override val compiler: Compiler) extends ObjectMetric {

  import global.TreeExtensions

  def overridingPatternVariables(tree: global.Tree): Int = tree.scopeCountTraverse(scopes => {
    case global.Bind(name, _) => scopes.exists(_.contains(name.toString.trim))
  })

  override def run(tree: global.ImplDef): List[MetricResult] = List(
    MetricResult("OverridingPatternVariables", overridingPatternVariables(tree))
  )
}
