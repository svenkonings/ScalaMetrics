package codeAnalysis.metrics.baseline

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{Metric, MetricProducer, MetricResult, ObjectMetric}

object DepthOfInheritance extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new DepthOfInheritance(compiler)
}

class DepthOfInheritance(override val compiler: Compiler) extends ObjectMetric {

  import global.SymbolExtensions

  def depthOfInheritance(symbol: global.Symbol): Int = {
    def recursiveDepth(symbol: global.Symbol): Int = {
      val name = symbol.qualifiedName
      if (name.equals("java.lang.Object") || name.equals("scala.Any"))
        0
      else
        1 + symbol.parentSymbols.map(recursiveDepth).maxOption.getOrElse(0)
    }

    recursiveDepth(symbol) - 1
  }

  override def run(tree: global.ImplDef): List[MetricResult] = {
    List(MetricResult("DepthOfInheritance", depthOfInheritance(tree.symbol)))
  }
}
