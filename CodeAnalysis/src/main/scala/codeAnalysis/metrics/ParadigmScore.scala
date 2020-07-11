package codeAnalysis.metrics

import codeAnalysis.analyser.Global
import codeAnalysis.analyser.metric.{MethodMetric, Metric, MetricProducer, MetricResult}

object ParadigmScore extends MetricProducer {
  override def apply(implicit global: Global): Metric = new ParadigmScore
}

//noinspection DuplicatedCode
class ParadigmScore(implicit val global: Global) extends MethodMetric {

  import global.TreeExtensions

  /**
   * F1: Checks whether the method is recursive or not
   */
  def recursive(tree: global.DefDef): Int = {
    val isRecursive = tree.exists {
      case apply: global.Apply if tree.symbol == apply.symbol => true
      case _ => false
    }
    if (isRecursive) 1 else 0
  }

  /**
   * F2: Checks whether the method has nested methods or not
   */
  def nested(tree: global.DefDef): Int = {
    val hasNestedMethods = tree.exists {
      case defdef: global.DefDef if tree != defdef => true
      case _ => false
    }
    if (hasNestedMethods) 1 else 0
  }

  /**
   * F3: Checks whether the method has higher-order parameters
   */
  def higherOrderParams(tree: global.DefDef): Int = {
    val hasHigherOrderParameters = tree.vparamss.exists(_.exists(_.isFunction))
    if (hasHigherOrderParameters) 1 else 0
  }

  /**
   * F4: Checks whether the method has calls to higher order functions
   */
  def higherOrderCalls(tree: global.DefDef): Int = {
    val hasHigherOrderCalls = tree.exists {
      case apply: global.Apply if apply.args.exists(_.isFunction) => true
      case _ => false
    }
    if (hasHigherOrderCalls) 1 else 0
  }

  /**
   * F5: Checks whether the tree returns a higher order type
   */
  def higherOrderReturn(tree: global.DefDef): Int = {
    val hasHigherOrderReturn = tree.isFunction
    if (hasHigherOrderReturn) 1 else 0
  }


  /**
   * F6: Checks whether the tree has calls returning partial functions
   */
  def currying(tree: global.DefDef): Int = {
    val hasCurrying = tree.exists {
      case apply: global.Apply if apply.isFunction => true
      case _ => false
    }
    if (hasCurrying) 1 else 0
  }

  /**
   * F3-6: Checks whether the tree contains functions
   */
  def functions(tree: global.DefDef): Int = {
    val hasFunctions = tree.exists(_.isFunction)
    if (hasFunctions) 1 else 0
  }

  /**
   * F7: Checks whether the tree contains pattern matching
   */
  def patternMatch(tree: global.DefDef): Int = {
    val hasPatternMatch = tree.exists {
      case _: global.Match => true
      case _ => false
    }
    if (hasPatternMatch) 1 else 0
  }

  /**
   * F8: Checks whether the tree uses lazy values
   */
  def lazyValues(tree: global.DefDef): Int = {
    val hasLazyValues = tree.exists(_.isLazy)
    if (hasLazyValues) 1 else 0
  }

  /**
   * O1: Checks whether the tree uses variables
   */
  def variables(tree: global.DefDef): Int = {
    val hasVariables = tree.exists(_.isVar)
    if (hasVariables) 1 else 0
  }

  /**
   * O2: Checks whether the tree contains calls resulting in Unit
   */
  def sideEffects(tree: global.DefDef): Int = {
    val hasSideEffects = tree.exists(_.isUnit)
    if (hasSideEffects) 1 else 0
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
    val o1 = variables(tree)
    val o2 = sideEffects(tree)
    List(
      MetricResult("Recursive", f1),
      MetricResult("Nested", f2),
      MetricResult("HigherOrderParams", f3),
      MetricResult("HigherOrderCalls", f4),
      MetricResult("HigherOrderReturn", f5),
      MetricResult("Currying", f6),
      MetricResult("Functions", f36),
      MetricResult("PatternMatch", f7),
      MetricResult("LazyValues", f8),
      MetricResult("Variables", o1),
      MetricResult("SideEffects", o2),
    )
  }
}
