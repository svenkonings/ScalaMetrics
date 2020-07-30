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
  def isRecursive(tree: global.DefDef): Int = tree.myExists {
    case apply: global.Apply => tree.symbol == apply.symbol
  }.toInt

  /**
   * Checks whether the method is nested or not
   */
  def isNested(tree: global.DefDef): Int = tree.symbol.owner.isMethod.toInt

  /**
   * F2: Checks whether the method has nested methods or not
   */
  def hasNestedMethods(tree: global.DefDef): Int = tree.myExists {
    case defdef: global.DefDef => tree != defdef
  }.toInt

  /**
   * F3: Checks whether the method has higher-order parameters
   */
  def hasHigherOrderParameters(tree: global.DefDef): Int =
    tree.vparamss.exists(_.exists(_.isFunction)).toInt

  /**
   * F4: Checks whether the method has calls to higher order functions
   */
  def hasHigherOrderCalls(tree: global.DefDef): Int = tree.myExists {
    case apply: global.Apply => apply.args.exists(_.isFunction)
  }.toInt

  /**
   * F5: Checks whether the tree returns a higher order type
   */
  def hasHigherOrderReturn(tree: global.DefDef): Int = tree.isFunction.toInt


  /**
   * F6: Checks whether the tree has calls returning partial functions
   */
  def hasCurrying(tree: global.DefDef): Int = tree.myExists {
    case apply: global.Apply => apply.isFunction
  }.toInt

  /**
   * F3-6: Checks whether the tree myExists functions
   */
  def hasFunctions(tree: global.DefDef): Int = tree.myExists(_.isFunction).toInt

  /**
   * F7: Checks whether the tree myExists pattern matching
   */
  def hasPatternMatching(tree: global.DefDef): Int = tree.myExists {
    case _: global.Match => true
  }.toInt

  /**
   * F8: Checks whether the tree uses lazy values
   */
  def hasLazyValues(tree: global.DefDef): Int = tree.myExists(_.isLazy).toInt

  /**
   * O1: Checks whether the tree uses variables
   */
  def hasVariables(tree: global.DefDef): Int = tree.myExists(_.isVar).toInt

  /**
   * O2: Checks whether the tree myExists calls resulting in Unit
   */
  def hasSideEffects(tree: global.DefDef): Int = tree.myExists(_.isUnit).toInt

  override def run(arg: Global#DefDef): List[MetricResult] = {
    val tree = arg.asInstanceOf[global.DefDef]
    val f1 = isRecursive(tree)
    val f2a = isNested(tree)
    val f2 = hasNestedMethods(tree)
    val f3 = hasHigherOrderParameters(tree)
    val f4 = hasHigherOrderCalls(tree)
    val f5 = hasHigherOrderReturn(tree)
    val f6 = hasCurrying(tree)
    val f36 = hasFunctions(tree)
    val f7 = hasPatternMatching(tree)
    val f8 = hasLazyValues(tree)
    val fScore = f1 + f2 + f3 + f4 + f5 + f6 + f7 + f8
    val o1 = hasVariables(tree)
    val o2 = hasSideEffects(tree)
    val oScore = o1 + o2
    val score = (fScore - oScore) \ (fScore + oScore)
    List(
      MetricResult("IsRecursive", f1),
      MetricResult("IsNested", f2a),
      MetricResult("IsHigherOrderMethod", f5),
      MetricResult("HasNestedMethods", f2),
      MetricResult("HasHigherOrderParameters", f3),
      MetricResult("HasHigherOrderCalls", f4),
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
