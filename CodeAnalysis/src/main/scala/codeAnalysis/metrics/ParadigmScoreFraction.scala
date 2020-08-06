package codeAnalysis.metrics

import codeAnalysis.analyser.FractionPart._
import codeAnalysis.analyser.Global
import codeAnalysis.analyser.metric.{MethodMetric, Metric, MetricProducer, MetricResult}
import codeAnalysis.util.Extensions._

object ParadigmScoreFraction extends MetricProducer {
  override def apply(global: Global): Metric = new ParadigmScoreFraction(global)
}

class ParadigmScoreFraction(val global: Global) extends MethodMetric {

  import global.TreeExtensions

  /**
   * FF1: The fraction of recursive calls to number of total calls
   */
  def fractionRecursiveCalls(tree: global.DefDef): Double = tree.fraction {
    case apply: global.Apply if tree.symbol == apply.symbol => Both
    case _: global.Apply => Denominator
  }

  /**
   * FF2: The fraction of nested methods to total number of values and methods
   */
  def FractionNestedMethods(tree: global.DefDef): Double = {
    tree.fraction {
      case defdef: global.DefDef if tree != defdef => Both
      case defdef: global.ValOrDefDef if tree != defdef => Denominator
    }
  }

  /**
   * FF4b: The fraction of function parameters to the total number of parameters
   */
  def fractionFunctionParameters(tree: global.DefDef): Double = {
    val params = tree.vparamss.flatten
    val higherOrderParamCount = params.count(_.isFunction)
    higherOrderParamCount \ params.size
  }

  /**
   * FF4c: The fraction of higher order functions calls to total number of calls
   */
  def fractionHigherOrderCalls(tree: global.DefDef): Double = tree.fraction {
    case apply: global.Apply if apply.args.exists(_.isFunction) => Both
    case _: global.Apply => Denominator
  }

  /**
   * FF4d: The fraction of functions calls to total number of calls
   */
  def fractionFunctionCalls(tree: global.DefDef): Double = tree.fraction {
    case apply: global.Apply if (apply.fun match {
      case select: global.Select => select.qualifier.isFunction
      case _ => false
    }) => Both
    case _: global.Apply => Denominator
  }

  /**
   * FF4e: The fraction of calls returning partial functions to total number of calls
   */
  def fractionCurrying(tree: global.DefDef): Double = tree.fraction {
    case apply: global.Apply if apply.isFunction => Both
    case _: global.Apply => Denominator
  }

  /**
   * FF6: The fraction of lazy value usage to total number of value usage
   */
  def fractionLazyValues(tree: global.DefDef): Double = tree.fraction {
    case tree@(_: global.ValDef | _: global.Ident | _: global.Assign | _: global.Bind | _: global.Select) => // TODO: Test this: if tree.symbol.isVal
      if (tree.isLazy) Both else Denominator
  }

  /**
   * FO1: The fraction of variable usage to total number of value usage
   */
  def fractionVariables(tree: global.DefDef): Double = tree.fraction {
    case tree@(_: global.ValDef | _: global.Ident | _: global.Assign | _: global.Bind | _: global.Select) =>
      if (tree.isVar) Both else Denominator
  }

  /**
   * FO1a: The fraction of variable usage to total number of value usage
   */
  def fractionVariableDefinitions(tree: global.DefDef): Double = tree.fraction {
    case tree: global.ValDef => if (tree.isVar) Both else Denominator
  }

  /**
   * FO2b: The fraction of calls resulting in Unit to the total number of calls
   */
  def fractionSideEffectCalls(tree: global.DefDef): Double = tree.fraction {
    case tree: global.Apply if tree.isUnit => Both
    case _: global.Apply => Denominator
  }

  /**
   * FO2c: The fraction of calls resulting in Unit to the total number of calls
   */
  def fractionSideEffectFunctions(tree: global.DefDef): Double = tree.fraction {
    case function: global.Function => if (function.body.isUnit) Both else Denominator
  }

  override def run(arg: Global#DefDef): List[MetricResult] = {
    val tree = arg.asInstanceOf[global.DefDef]
    val f1 = fractionRecursiveCalls(tree)
    val f2 = FractionNestedMethods(tree)
    val f4b = fractionFunctionParameters(tree)
    val f4c = fractionHigherOrderCalls(tree)
    val f4d = fractionFunctionCalls(tree)
    val f4e = fractionCurrying(tree)
    val f6 = fractionLazyValues(tree)

    val fScore = f1 + f2 + f4b + f4c + f4d + f4e + f6

    val o1 = fractionVariables(tree)
    val o1a = fractionVariableDefinitions(tree)
    val o2b = fractionSideEffectCalls(tree)
    val o2c = fractionSideEffectFunctions(tree)

    val oScore = o1 + o1a + o2b + o2c

    val score = (fScore - oScore) \ (fScore + oScore)

    List(
      MetricResult("FractionRecursiveCalls", f1),
      MetricResult("FractionNestedMethods", f2),
      MetricResult("FractionFunctionParameters", f4b),
      MetricResult("FractionHigherOrderCalls", f4c),
      MetricResult("FractionFunctionCalls", f4d),
      MetricResult("FractionCurrying", f4e),
      MetricResult("FractionLazyValues", f6),

      MetricResult("FunctionalScoreFraction", fScore),

      MetricResult("FractionVariables", o1),
      MetricResult("FractionVariableDefinitions", o1a),
      MetricResult("FractionSideEffectCalls", o2b),
      MetricResult("FractionSideEffectCalls", o2c),

      MetricResult("ImperativeScoreFraction", oScore),

      MetricResult("ParadigmScoreFraction", score),
    )
  }
}
