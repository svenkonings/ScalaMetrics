package codeAnalysis.metrics.baseline

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{Metric, MetricProducer, MetricResult, ObjectMetric}

import scala.util.matching.Regex

object NumberOfChildren extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new NumberOfChildren(compiler)
}

class NumberOfChildren(override val compiler: Compiler) extends ObjectMetric {

  import global.{SymbolExtensions, TreeExtensions}

  def numberOfChildren(tree: global.ImplDef): Int = {
    val name = tree.symbol.nameString
    val qualifiedName = tree.symbol.qualifiedName
    val nameFilter = s"(extends|with)\\s+${Regex.quote(name)}".r
    compiler.loadedSources
      .filter(source => nameFilter.findFirstIn(new String(source.content)).isDefined)
      .map(source => compiler.treeFromLoadedSource(source).asInstanceOf[global.Tree])
      .filter(_ != null)
      .map(_.count { case tree: global.ImplDef => tree.symbol.parentSymbols.exists(_.qualifiedName == qualifiedName) })
      .sum
  }

  override def run(tree: global.ImplDef): List[MetricResult] =
    List(MetricResult("NumberOfChildren", numberOfChildren(tree)))
}
