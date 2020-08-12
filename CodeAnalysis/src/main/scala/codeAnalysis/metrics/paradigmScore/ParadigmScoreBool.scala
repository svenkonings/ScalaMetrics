package codeAnalysis.metrics.paradigmScore

import codeAnalysis.analyser.Global
import codeAnalysis.analyser.metric.{MethodMetric, Metric, MetricProducer, MetricResult}
import codeAnalysis.util.Extensions._

object ParadigmScoreBool extends MetricProducer {
  override def apply(global: Global): Metric = new ParadigmScoreBool(global)
}

class ParadigmScoreBool(val global: Global) extends MethodMetric {

  import global.TreeExtensions

  /**
   * BF1: Checks whether the method is recursive or not
   */
  def isRecursive(tree: global.DefDef): Int = tree.myExists {
    case apply: global.Apply => tree.symbol == apply.symbol
  }.toInt

  /**
   * BF2: Checks whether the method is nested or not
   */
  def isNested(tree: global.DefDef): Int = tree.symbol.owner.isMethod.toInt

  /**
   * BF3: Checks whether the method has nested methods or not
   */
  def hasNestedMethods(tree: global.DefDef): Int = tree.myExists {
    case defdef: global.DefDef => tree != defdef
  }.toInt

  /**
   * BF4: Checks whether the tree contains functions
   */
  def hasFunctions(tree: global.DefDef): Int = tree.myExists(_.isFunction).toInt

  /**
   * BF4a: Checks whether the tree returns a higher order type
   */
  def isFunction(tree: global.DefDef): Int = tree.isFunction.toInt

  /**
   * BF4b: Checks whether the method has higher-order parameters
   */
  def hasFunctionParameters(tree: global.DefDef): Int =
    tree.vparamss.exists(_.exists(_.isFunction)).toInt

  /**
   * BF4c: Checks whether the method has calls to higher order functions
   */
  def hasHigherOrderCalls(tree: global.DefDef): Int = tree.myExists {
    case apply: global.Apply => apply.args.exists(_.isFunction)
  }.toInt

  /**
   * BF4d: Checks whether the method has calls to functions
   */
  def hasFunctionCalls(tree: global.DefDef): Int = tree.myExists {
    case apply: global.Apply => apply.fun match {
      case select: global.Select => select.qualifier.isFunction
      case _ => false
    }
  }.toInt

  /**
   * BF4e: Checks whether the tree has calls returning partial functions
   */
  def hasCurrying(tree: global.DefDef): Int = tree.myExists {
    case apply: global.Apply => apply.isFunction
  }.toInt

  /**
   * BF5: Checks whether the tree contains pattern matching
   */
  def hasPatternMatching(tree: global.DefDef): Int = tree.myExists {
    case _: global.Match => true
  }.toInt

  /**
   * BF6: Checks whether the tree uses lazy values
   */
  def hasLazyValues(tree: global.DefDef): Int = tree.myExists(_.isLazy).toInt

  /**
   * BF7: Checks whether the tree uses multiple parameter lists
   */
  def hasMultipleParameterLists(tree: global.DefDef): Int = (tree.vparamss.size > 1).toInt

  /**
   * BO1: Checks whether the tree uses variables
   */
  def hasVariables(tree: global.DefDef): Int = tree.myExists(_.isVar).toInt

  /**
   * BO1a: Checks whether the tree defines variables
   */
  def hasVariableDefinitions(tree: global.DefDef): Int = tree.myExists {
    case tree: global.ValDef => tree.isVar
  }.toInt

  /**
   * BO1b: Checks whether the tree assignes inner variables
   */
  def hasInnerVariableAssignment(tree: global.DefDef): Int = tree.myExists {
    case _: global.Assign => true // Inner variable assign
  }.toInt

  /**
   * BO1c: Checks whether the tree selects outer variables
   */
  def hasOuterVariableUsage(tree: global.DefDef): Int = tree.myExists {
    case tree: global.Select => tree.isVar
  }.toInt

  /**
   * BO1d: Checks whether the tree assignes outer variables
   */
  def hasOuterVariableAssignment(tree: global.DefDef): Int = tree.myExists {
    case tree: global.Select => tree.name.endsWith("_$eq") // Outer variable assign
  }.toInt

  /**
   * BO2: Checks whether the tree constians Unit types
   */
  def hasSideEffects(tree: global.DefDef): Int = tree.myExists(_.isUnit).toInt

  /**
   * BO2a: Checks whether the tree returns Unit
   */
  def isSideEffect(tree: global.DefDef): Int = tree.isUnit.toInt

  /**
   * BO2b: Checks whether the tree contains calls resulting in Unit
   */
  def hasSideEffectCalls(tree: global.DefDef): Int = tree.myExists {
    case apply: global.Apply => apply.isUnit
    case _: global.Assign => true
  }.toInt

  /**
   * BO2c: Checks whether the tree uses functions resulting in Unit
   */
  def hasSideEffectFunctions(tree: global.DefDef): Int = tree.myExists {
    case function: global.Function => function.body.isUnit
  }.toInt

  override def run(arg: Global#DefDef): List[MetricResult] = {
    val tree = arg.asInstanceOf[global.DefDef]
    val f1 = isRecursive(tree)
    val f2 = isNested(tree)
    val f3 = hasNestedMethods(tree)
    val f4 = hasFunctions(tree)
    val f4a = isFunction(tree)
    val f4b = hasFunctionParameters(tree)
    val f4c = hasHigherOrderCalls(tree)
    val f4d = hasFunctionCalls(tree)
    val f4e = hasCurrying(tree)
    val f5 = hasPatternMatching(tree)
    val f6 = hasLazyValues(tree)
    val f7 = hasMultipleParameterLists(tree)

    val fScore = f1 + f2 + f3 + f4 + f4a + f4b + f4c + f4d + f4e + f5 + f6 + f7

    val o1 = hasVariables(tree)
    val o1a = hasVariableDefinitions(tree)
    val o1b = hasInnerVariableAssignment(tree)
    val o1c = hasOuterVariableUsage(tree)
    val o1d = hasOuterVariableAssignment(tree)
    val o2 = hasSideEffects(tree)
    val o2a = isSideEffect(tree)
    val o2b = hasSideEffectCalls(tree)
    val o2c = hasSideEffectFunctions(tree)

    val oScore = o1 + o1a + o1b + o1c + o1d + o2 + o2c + o2b + o2a

    val score = (fScore - oScore) \ (fScore + oScore)

    List(
      MetricResult("IsRecursive", f1),
      MetricResult("IsNested", f2),
      MetricResult("HasNestedMethods", f3),
      MetricResult("HasFunctions", f4),
      MetricResult("IsFunction", f4a),
      MetricResult("HasFunctionParameters", f4b),
      MetricResult("HasHigherOrderCalls", f4c),
      MetricResult("HasFunctionCalls", f4d),
      MetricResult("HasCurrying", f4e),
      MetricResult("HasPatternMatching", f5),
      MetricResult("HasLazyValues", f6),
      MetricResult("HasMultipleParameterLists", f7),

      MetricResult("FunctionalScoreBool", fScore),

      MetricResult("HasVariables", o1),
      MetricResult("HasVariableDefinitions", o1a),
      MetricResult("HasInnerVariableAssignment", o1b),
      MetricResult("HasOuterVariableUsage", o1c),
      MetricResult("HasOuterVariableAssignment", o1d),
      MetricResult("HasSideEffects", o2),
      MetricResult("IsSideEffect", o2a),
      MetricResult("HasSideEffectCalls", o2b),
      MetricResult("HasSideEffectFunctions", o2c),

      MetricResult("ImperativeScoreBool", oScore),

      MetricResult("ParadigmScoreBool", score),
    )
  }
}
