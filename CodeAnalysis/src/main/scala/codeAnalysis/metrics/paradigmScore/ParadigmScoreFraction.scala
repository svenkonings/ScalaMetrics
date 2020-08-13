package codeAnalysis.metrics.paradigmScore

import codeAnalysis.analyser.Global
import codeAnalysis.analyser.metric.{MethodMetric, Metric, MetricProducer, MetricResult}
import codeAnalysis.util.Extensions._

object ParadigmScoreFraction extends MetricProducer {
  override def apply(global: Global): Metric = new ParadigmScoreFraction(global)
}

class ParadigmScoreFraction(val global: Global) extends MethodMetric {

  import global.TreeExtensions

  /**
   * FF1: The fraction of lines with recursive method calls
   */
  def fractionRecursiveCalls(tree: global.DefDef): Int = tree.lines {
    case apply: global.Apply => tree.symbol == apply.symbol
  }

  /**
   * BF2: Checks whether the method is nested or not
   */
  def isNested(tree: global.DefDef): Int = tree.symbol.owner.isMethod.toInt

  /**
   * FF3: The fraction of lines with nested methods
   */
  def fractionNestedMethods(tree: global.DefDef): Int = tree.lines {
    case defdef: global.DefDef => tree != defdef
  }

  /**
   * FF4: The fraction of lines with functions
   */
  def fractionFunctions(tree: global.DefDef): Int = tree.lines(_.isFunction)

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
   * FF4c: The fraction of lines with calls to higher order functions
   */
  def fractionHigherOrderCalls(tree: global.DefDef): Int = tree.lines {
    case apply: global.Apply => apply.args.exists(_.isFunction)
  }

  /**
   * FF4d: The fraction of lines with calls to functions
   */
  def fractionFunctionCalls(tree: global.DefDef): Int = tree.lines {
    case apply: global.Apply => apply.fun match {
      case select: global.Select => select.qualifier.isFunction
      case _ => false
    }
  }

  /**
   * FF4e: The fraction of lines with calls returning partial functions
   */
  def fractionCurrying(tree: global.DefDef): Int = tree.lines {
    case apply: global.Apply => apply.isFunction
  }

  /**
   * FF5: The fraction of lines with pattern matches
   */
  def fractionPatternMatching(tree: global.DefDef): Int = tree.lines {
    case _: global.Match => true
  }

  /**
   * FF6: The fraction of lines with lazy value usage
   */
  def fractionLazyValues(tree: global.DefDef): Int = tree.lines(_.isLazy)

  /**
   * BF7: Checks whether the tree uses multiple parameter lists
   */
  def hasMultipleParameterLists(tree: global.DefDef): Int = (tree.vparamss.size > 1).toInt

  /**
   * FO1: The fraction of lines with variable usage
   */
  def fractionVariables(tree: global.DefDef): Int = tree.lines(_.isVar)

  /**
   * FO1a: The fraction of lines with variable definitions
   */
  def fractionVariableDefinitions(tree: global.DefDef): Int = tree.lines {
    case tree: global.ValDef => tree.isVar
  }

  /**
   * FO1b: The fraction of lines with assignesnments to inner variables
   */
  def fractionInnerVariableAssignment(tree: global.DefDef): Int = tree.lines {
    case _: global.Assign => true // Inner variable assign
  }

  /**
   * FO1c: The fraction of lines with outer variable references
   */
  def fractionOuterVariableUsage(tree: global.DefDef): Int = tree.lines {
    case tree: global.Select => tree.isVar
  }

  /**
   * FO1d: The fraction of lines with outer variable assignments
   */
  def fractionOuterVariableAssignment(tree: global.DefDef): Int = tree.lines {
    case tree: global.Select => tree.name.endsWith("_$eq") // Outer variable assign
  }

  /**
   * FO2: The fraction of lines with calls resulting in Unit
   */
  def fractionSideEffects(tree: global.DefDef): Int = tree.lines(_.isUnit)

  /**
   * BO2a: Checks whether the tree returns Unit
   */
  def isSideEffect(tree: global.DefDef): Int = tree.isUnit.toInt

  /**
   * FO2b: The fraction of lines with calls resulting in Unit
   */
  def fractionSideEffectCalls(tree: global.DefDef): Int = tree.lines {
    case apply: global.Apply => apply.isUnit
    case _: global.Assign => true
  }

  /**
   * FO2c: The fraction of lines with functions resulting in Unit
   */
  def fractionSideEffectFunctions(tree: global.DefDef): Int = tree.lines {
    case function: global.Function => function.body.isUnit
  }

  override def run(tree: global.DefDef): List[MetricResult] = {
    val total = tree.lines(_ => true)

    val f1 = fractionRecursiveCalls(tree) \ total
    val f2 = isNested(tree)
    val f3 = fractionNestedMethods(tree) \ total
    val f4 = fractionFunctions(tree) \ total
    val f4a = isFunction(tree)
    val f4b = hasFunctionParameters(tree)
    val f4c = fractionHigherOrderCalls(tree) \ total
    val f4d = fractionFunctionCalls(tree) \ total
    val f4e = fractionCurrying(tree) \ total
    val f5 = fractionPatternMatching(tree) \ total
    val f6 = fractionLazyValues(tree) \ total
    val f7 = hasMultipleParameterLists(tree)

    val fScore = f1 + f2 + f3 + f4 + f4a + f4b + f4c + f4d + f4e + f5 + f6 + f7

    val o1 = fractionVariables(tree) \ total
    val o1a = fractionVariableDefinitions(tree) \ total
    val o1b = fractionInnerVariableAssignment(tree) \ total
    val o1c = fractionOuterVariableUsage(tree) \ total
    val o1d = fractionOuterVariableAssignment(tree) \ total
    val o2 = fractionSideEffects(tree) \ total
    val o2a = isSideEffect(tree)
    val o2b = fractionSideEffectCalls(tree) \ total
    val o2c = fractionSideEffectFunctions(tree) \ total

    val oScore = o1 + o1a + o1b + o1c + o1d + o2 + o2c + o2b + o2a

    val hasPoints = (fScore != 0 || oScore != 0).toInt
    val score = (fScore - oScore) \ (fScore + oScore)

    List(
      MetricResult("FractionRecursiveCalls", f1),
      MetricResult("FractionNestedMethods", f3),
      MetricResult("FractionFunctions", f4),
      MetricResult("FractionHigherOrderCalls", f4c),
      MetricResult("FractionFunctionCalls", f4d),
      MetricResult("FractionCurrying", f4e),
      MetricResult("FractionPatternMatching", f5),
      MetricResult("FractionLazyValues", f6),

      MetricResult("FunctionalScoreFraction", fScore),

      MetricResult("FractionVariables", o1),
      MetricResult("FractionVariableDefinitions", o1a),
      MetricResult("FractionInnerVariableAssignment", o1b),
      MetricResult("FractionOuterVariableUsage", o1c),
      MetricResult("FractionOuterVariableAssignment", o1d),

      MetricResult("FractionSideEffects", o2),
      MetricResult("FractionSideEffectCalls", o2b),
      MetricResult("FractionSideEffectFunctions", o2c),

      MetricResult("ImperativeScoreFraction", oScore),

      MetricResult("HasPointsFraction", hasPoints),
      MetricResult("ParadigmScoreFraction", score),
    )
  }
}
