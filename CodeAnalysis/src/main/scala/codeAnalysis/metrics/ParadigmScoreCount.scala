package codeAnalysis.metrics

import codeAnalysis.analyser.Global
import codeAnalysis.analyser.metric.{MethodMetric, Metric, MetricProducer, MetricResult}
import codeAnalysis.util.Extensions._

object ParadigmScoreCount extends MetricProducer {
  override def apply(global: Global): Metric = new ParadigmScoreCount(global)
}

//noinspection DuplicatedCode
class ParadigmScoreCount(val global: Global) extends MethodMetric {

  import global.TreeExtensions

  /**
   * F1: Counts the number of recursive method calls
   */
  def recursive(tree: global.DefDef): Int = tree.count {
    case apply: global.Apply  => tree.symbol == apply.symbol
  }

  /**
   * F2: Counts the number of nested methods
   */
  def nested(tree: global.DefDef): Int = tree.count {
    case defdef: global.DefDef  => tree != defdef
  }

  /**
   * F3: Counts the number of higher-order parameters
   */
  def higherOrderParams(tree: global.DefDef): Int =
    tree.vparamss.map(_.count(_.isFunction)).sum

  /**
   * F4: Counts the number of calls to higher order functions
   */
  def higherOrderCalls(tree: global.DefDef): Int = tree.count {
    case apply: global.Apply => apply.args.exists(_.isFunction)
  }

  /**
   * F5: Checks whether the tree returns a higher order type
   */
  def higherOrderReturn(tree: global.DefDef): Int = tree.isFunction.toInt
// FIXME: Missing Function values, calls and definitions, See usesFunction

  /**
   * F6: Counts the number of calls returning partial functions
   */
  def currying(tree: global.DefDef): Int = tree.count {
    case apply: global.Apply => apply.isFunction
  }

  /**
   * F3-6: Counts the number of functions
   */
  def functions(tree: global.DefDef): Int = tree.count{
    case term @ (_: global.TermTree | _: global.SymTree) => term.isFunction
  }

  /**
   * F7: Counts the number of pattern matches
   */
  def patternMatch(tree: global.DefDef): Int = tree.count {
    case _: global.Match => true
  }

  /**
   * F8: Counts the number of lazy value usage
   */
  def lazyValues(tree: global.DefDef): Int = tree.count{
    case term @ (_: global.TermTree | _: global.SymTree) => term.isLazy
  }

  /**
   * O1: Counts the number of variable usage
   */
  def variables(tree: global.DefDef): Int = tree.count {
    case term @ (_: global.TermTree | _: global.SymTree) => term.isVar
  }

  /**
   * O2: Counts the number of calls resulting in Unit
   */
  def sideEffects(tree: global.DefDef): Int = tree.count {
    case tree @ (_: global.TermTree | _: global.SymTree) => tree.isUnit
  }

  override def run(arg: Global#DefDef): List[MetricResult] = {
    val tree = arg.asInstanceOf[global.DefDef]
    val f1 = recursive(tree)
    val f2 = nested(tree)
    val f3 = higherOrderParams(tree)
    val f4 = higherOrderCalls(tree)
    val f5 = higherOrderReturn(tree)
    val f6 = currying(tree)
    val f36 = functions(tree)
    val f7 = patternMatch(tree)
    val f8 = lazyValues(tree)
    val fScore = f1 + f2 + f3 + f4 + f5 + f6 + f7 + f8
    val o1 = variables(tree)
    val o2 = sideEffects(tree)
    val oScore = o1 + o2
    val score = (fScore - oScore) \ (fScore + oScore)
    List(
      MetricResult("CountRecursiveCalls", f1),
      MetricResult("CountNestedMethods", f2),
      MetricResult("CountHigherOrderParameters", f3),
      MetricResult("CountHigherOrderCalls", f4),
      // MetricResult("HasHigherOrderReturn", f5), // Same as ParadigmScoreBool
      MetricResult("CountCurrying", f6),
      MetricResult("CountFunctions", f36),
      MetricResult("CountPatternMatching", f7),
      MetricResult("CountLazyValues", f8),
      MetricResult("FunctionalScoreCount", fScore),
      MetricResult("CountVariables", o1),
      MetricResult("CountSideEffects", o2),
      MetricResult("ImperativeScoreCount", oScore),
      MetricResult("ParadigmScoreCount", score),
    )
  }
}
