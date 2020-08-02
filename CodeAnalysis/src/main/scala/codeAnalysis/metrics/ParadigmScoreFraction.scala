package codeAnalysis.metrics

import codeAnalysis.analyser.FractionPart._
import codeAnalysis.analyser.Global
import codeAnalysis.analyser.metric.{MethodMetric, Metric, MetricProducer, MetricResult}
import codeAnalysis.util.Extensions._

object ParadigmScoreFraction extends MetricProducer {
  override def apply(global: Global): Metric = new ParadigmScoreFraction(global)
}

//noinspection DuplicatedCode
class ParadigmScoreFraction(val global: Global) extends MethodMetric {

  import global.TreeExtensions

  /**
   * F1: The fraction of recursive calls to number of total calls
   */
  def recursive(tree: global.DefDef): Double = tree.fraction {
    case apply: global.Apply if tree.symbol == apply.symbol => Both
    case _: global.Apply => Denominator
  }

  /**
   * F2: The fraction of nested methods to total number of values and methods
   */
  def nested(tree: global.DefDef): Double = {
    tree.fraction {
      case defdef: global.DefDef if tree != defdef => Both
      case defdef: global.ValOrDefDef if tree != defdef => Denominator
    }
  }

  /**
   * F3: The fraction of higher-order parameters to the total number of parameters
   */
  def higherOrderParams(tree: global.DefDef): Double = {
    val params = tree.vparamss.flatten
    val higherOrderParamCount = params.count(_.isFunction)
    higherOrderParamCount \ params.size
  }

  /**
   * F4: The fraction of higher order functions calls to total number of calls
   */
  def higherOrderCalls(tree: global.DefDef): Double = tree.fraction {
    case apply: global.Apply if apply.args.exists(_.isFunction) => Both
    case _: global.Apply => Denominator
  }

  /**
   * F5: Checks whether the tree returns a higher order type
   */
  def higherOrderReturn(tree: global.DefDef): Int = tree.isFunction.toInt


  /**
   * F6: The fraction of calls returning partial functions to total number of calls
   */
  def currying(tree: global.DefDef): Double = tree.fraction {
    case apply: global.Apply if apply.isFunction => Both
    case _: global.Apply => Denominator
  }

  /**
   * F3-6: The fraction of terms with a function type to the total number of terms
   */
  def functions(tree: global.DefDef): Double = tree.fraction {
    case tree: global.TermTree if tree.isFunction => Both
    case _: global.TermTree => Denominator
  }

  /**
   * F7: The fraction of pattern matches to total number of terms
   */
  def patternMatch(tree: global.DefDef): Double = tree.fraction {
    case _: global.Match => Both
    case _: global.TermTree => Denominator
  }

  /**
   * F8: The fraction of lazy value usage to total number of value usage
   */
  def lazyValues(tree: global.DefDef): Double = tree.fraction {
    case tree @ (_: global.ValDef | _: global.Ident | _: global.Assign | _: global.Bind | _:global.Select) => // TODO: Change to tree.isVal
      if (tree.isLazy) Both else Denominator
  }

  /**
   * O1: The fraction of variable usage to total number of value usage
   */
  def variables(tree: global.DefDef): Double = tree.fraction {
    case tree @ (_: global.ValDef | _: global.Ident | _: global.Assign | _: global.Bind | _:global.Select) =>
      if (tree.isVar) Both else Denominator
  }

  /**
   * O2: The fraction of calls resulting in Unit to the total number of calls
   */
  def sideEffects(tree: global.DefDef): Double = tree.fraction {
    case tree: global.Apply if tree.isUnit => Both
    case _: global.Apply => Denominator
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
    val funcs = tree.collect {
      case tree if tree.isFunction => tree
    }
    val lazys = tree.collect {
      case tree if tree.isLazy => tree
    }
    val units = tree.collect {
      case tree if tree.isUnit => tree
    }
    val vars = tree.collect {
      case tree if tree.isVar => tree
    }
    List(
      MetricResult("FractionRecursiveCalls", f1),
      MetricResult("FractionNestedMethods", f2),
      MetricResult("FractionHigherOrderParams", f3),
      MetricResult("FractionHigherOrderCalls", f4),
      // MetricResult("HasHigherOrderReturn", f5), // Same as ParadigmScoreBool
      MetricResult("FractionCurrying", f6),
      MetricResult("FractionFunctions", f36),
      MetricResult("FractionPatternMatching", f7),
      MetricResult("FractionLazyValues", f8),
      MetricResult("FunctionalScoreFraction", fScore),
      MetricResult("FractionVariables", o1),
      MetricResult("FractionSideEffects", o2),
      MetricResult("ImperativeScoreFraction", oScore),
      MetricResult("ParadigmScoreFraction", score),
    )
  }
}
