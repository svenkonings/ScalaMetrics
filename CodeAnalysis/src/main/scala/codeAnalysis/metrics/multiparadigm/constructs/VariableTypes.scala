package codeAnalysis.metrics.multiparadigm.constructs

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{Metric, MetricProducer, MetricResult, ObjectMetric}

object VariableTypes extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new VariableTypes(compiler)
}

class VariableTypes(override val compiler: Compiler) extends ObjectMetric {

  import global.TreeExtensions

  def unitVariables(tree: global.Tree): Int = tree.countTraverse {
    case tree: global.ValDef => tree.isUnit
  }

  def functionVariables(tree: global.Tree): Int = tree.countTraverse {
    case tree: global.ValDef => tree.isFunction
  }

  override def run(tree: global.ImplDef): List[MetricResult] = List(
    MetricResult("UnitVariables", unitVariables(tree)),
    MetricResult("FunctionVariables", functionVariables(tree)),
  )
}
