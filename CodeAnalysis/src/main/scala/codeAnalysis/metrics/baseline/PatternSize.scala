package codeAnalysis.metrics.baseline

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{MethodMetric, Metric, MetricProducer, MetricResult}

object PatternSize extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new PatternSize(compiler)
}

class PatternSize(override val compiler: Compiler) extends MethodMetric {

  import global.TreeExtensions

  def patternSize(tree: global.DefDef): Int = tree.sum {
    case tree: global.CaseDef => tree.pat.count(_ => true)
  }

  def patternVariables(tree: global.DefDef): Int = tree.count {
    case tree: global.Bind => true
  }

  override def run(tree: global.DefDef): List[MetricResult] = List(
    MetricResult("PatternSize", patternSize(tree)),
    MetricResult("NumberOfPatternVariables", patternVariables(tree))
  )
}
