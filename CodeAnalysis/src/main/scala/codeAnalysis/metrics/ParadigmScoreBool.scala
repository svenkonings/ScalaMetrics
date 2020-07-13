package codeAnalysis.metrics

import codeAnalysis.analyser.Global
import codeAnalysis.analyser.metric.{MethodMetric, Metric, MetricProducer, MetricResult}
import codeAnalysis.util.Extensions._

object ParadigmScoreBool extends MetricProducer {
  override def apply(implicit global: Global): Metric = new ParadigmScoreBool
}

//noinspection DuplicatedCode
class ParadigmScoreBool(implicit val global: Global) extends MethodMetric {

  import global.TreeExtensions

  /**
   * F1: Checks whether the method is recursive or not
   */
  def recursive(tree: global.DefDef): Int = tree.contains {
    case apply: global.Apply => tree.symbol == apply.symbol
  }.toInt

  /**
   * F2: Checks whether the method has nested methods or not
   */
  def nested(tree: global.DefDef): Int = tree.contains {
    case defdef: global.DefDef => tree != defdef
  }.toInt

  /**
   * F3: Checks whether the method has higher-order parameters
   */
  def higherOrderParams(tree: global.DefDef): Int =
    tree.vparamss.exists(_.exists(_.isFunction)).toInt

  /**
   * F4: Checks whether the method has calls to higher order functions
   */
  def higherOrderCalls(tree: global.DefDef): Int = tree.contains {
    case apply: global.Apply => apply.args.exists(_.isFunction)
  }.toInt

  /**
   * F5: Checks whether the tree returns a higher order type
   */
  def higherOrderReturn(tree: global.DefDef): Int = tree.isFunction.toInt


  /**
   * F6: Checks whether the tree has calls returning partial functions
   */
  def currying(tree: global.DefDef): Int = tree.contains {
    case apply: global.Apply => apply.isFunction
  }.toInt

  /**
   * F3-6: Checks whether the tree contains functions
   */
  def functions(tree: global.DefDef): Int = tree.exists(_.isFunction).toInt

  /**
   * F7: Checks whether the tree contains pattern matching
   */
  def patternMatch(tree: global.DefDef): Int = tree.contains {
    case _: global.Match => true
  }.toInt

  /**
   * F8: Checks whether the tree uses lazy values
   */
  def lazyValues(tree: global.DefDef): Int = tree.exists(_.isLazy).toInt

  /**
   * O1: Checks whether the tree uses variables
   */
  def variables(tree: global.DefDef): Int = tree.exists(_.isVar).toInt

  /**
   * O2: Checks whether the tree contains calls resulting in Unit
   */
  def sideEffects(tree: global.DefDef): Int = tree.exists(_.isUnit).toInt

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
    val score = fScore \ 8 - oScore \ 2
    List(
      MetricResult("IsRecursive", f1),
      MetricResult("HasNestedMethods", f2),
      MetricResult("HasHigherOrderParams", f3),
      MetricResult("HasHigherOrderCalls", f4),
      MetricResult("HasHigherOrderReturn", f5),
      MetricResult("HasCurrying", f6),
      MetricResult("HasFunctions", f36),
      MetricResult("HasPatternMatching", f7),
      MetricResult("HasLazyValues", f8),
      MetricResult("FunctionalScoreBool", fScore),
      MetricResult("HasVariables", o1),
      MetricResult("HasSideEffects", o2),
      MetricResult("ImperativeScoreBool", oScore),
      MetricResult("ParadigmScoreBool", score),
    )
  }
}
