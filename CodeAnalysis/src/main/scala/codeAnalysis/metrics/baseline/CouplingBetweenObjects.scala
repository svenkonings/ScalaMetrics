package codeAnalysis.metrics.baseline

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{Metric, MetricProducer, MetricResult, ObjectMetric}
import codeAnalysis.util.Constants.basicTypes

import scala.util.matching.Regex

object CouplingBetweenObjects extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new CouplingBetweenObjects(compiler)
}

class CouplingBetweenObjects(override val compiler: Compiler) extends ObjectMetric {

  import global.{SymbolExtensions, TreeExtensions}

  def fanOut(tree: global.ImplDef): Set[global.Symbol] = filterSymbols(tree)(tree.myCollect {
    case tree if tree.getTypeSymbol != null => tree.getTypeSymbol
  })

  def fanIn(tree: global.ImplDef): Set[global.Symbol] = {
    val name = tree.symbol.nameString
    val qualifiedName = tree.symbol.qualifiedName
    val nameFilter = Regex.quote(name).r
    filterSymbols(tree)(compiler.treesFromFilteredSources(nameFilter).flatMap(_.asInstanceOf[global.Tree].myCollect {
        case tree: global.ImplDef if tree.myExists {
          case tree if tree.symbol != null => tree.symbol.qualifiedName == qualifiedName
        } => tree.symbol
      }))
  }

  def filterSymbols(tree: global.ImplDef)(symbols: List[global.Symbol]): Set[global.Symbol] = symbols.toSet.filterNot {
    case _: global.NoSymbol => true
    case _: global.PackageClassSymbol => true
    case symbol => tree.symbol == symbol || basicTypes(symbol.qualifiedName)
  }

  override def run(tree: global.ImplDef): List[MetricResult] = {
    val out = fanOut(tree)
    val in = fanIn(tree)
    val total = out ++ in
    List(
      MetricResult("FanOut", out.size),
      MetricResult("FanIn", in.size),
      MetricResult("CouplingBetweenObjects", total.size)
    )
  }
}
