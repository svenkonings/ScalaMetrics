package codeAnalysis.metrics.baseline

import codeAnalysis.analyser.Global
import codeAnalysis.analyser.metric.{Metric, MetricProducer, MetricResult, ObjectMetric}

object DepthOfInheritance extends MetricProducer {
  override def apply(global: Global): Metric = new DepthOfInheritance(global)
}

class DepthOfInheritance(val global: Global) extends ObjectMetric {

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

  override def run(arg: Global#ImplDef): List[MetricResult] = {
    val tree = arg.asInstanceOf[global.ImplDef]
    List(MetricResult("DepthOfInheritance", depthOfInheritance(tree.symbol)))
  }
}
