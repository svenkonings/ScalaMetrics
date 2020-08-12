package codeAnalysis.metrics.paradigmScore

import codeAnalysis.analyser.Global
import codeAnalysis.analyser.metric.{MethodMetric, Metric, MetricProducer, MetricResult}
import codeAnalysis.util.Extensions._

object ParadigmScoreCount extends MetricProducer {
  override def apply(global: Global): Metric = new ParadigmScoreCount(global)
}

class ParadigmScoreCount(val global: Global) extends MethodMetric {

  import global.TreeExtensions

  /**
   * CF1: Counts the number of recursive method calls
   */
  def countRecursiveCalls(tree: global.DefDef): Int = tree.count {
    case apply: global.Apply => tree.symbol == apply.symbol
  }

  /**
   * CF2: Count the nesting depth of the method
   */
  def countNestedDepth(tree: global.DefDef): Int = {
    var count = 0
    var symbol = tree.symbol.owner
    while (symbol.isMethod) {
      count += 1
      symbol = symbol.owner
    }
    count
  }

  /**
   * CF3: Counts the number of nested methods
   */
  def countNestedMethods(tree: global.DefDef): Int = tree.count {
    case defdef: global.DefDef => tree != defdef
  }

  /**
   * CF4: Counts the number of functions
   */
  def countFunctions(tree: global.DefDef): Int = tree.count {
    case term@(_: global.TermTree | _: global.SymTree) => term.isFunction
  }

  /**
   * BF4a: Checks whether the tree returns a higher order type
   */
  def isFunction(tree: global.DefDef): Int = tree.isFunction.toInt

  /**
   * CF4b: Counts the number of higher-order parameters
   */
  def countFunctionParameters(tree: global.DefDef): Int =
    tree.vparamss.map(_.count(_.isFunction)).sum

  /**
   * CF4c: Counts the number of calls to higher order functions
   */
  def countHigherOrderCalls(tree: global.DefDef): Int = tree.count {
    case apply: global.Apply => apply.args.exists(_.isFunction)
  }

  /**
   * CF4d: Counts the number of calls to functions
   */
  def countFunctionCalls(tree: global.DefDef): Int = tree.count {
    case apply: global.Apply => apply.fun match {
      case select: global.Select => select.qualifier.isFunction
      case _ => false
    }
  }

  /**
   * CF4e: Counts the number of calls returning partial functions
   */
  def countCurrying(tree: global.DefDef): Int = tree.count {
    case apply: global.Apply => apply.isFunction
  }

  /**
   * CF5: Counts the number of pattern matches
   */
  def countPatternMatching(tree: global.DefDef): Int = tree.count {
    case _: global.Match => true
  }

  /**
   * CF6: Counts the number of lazy value usage
   */
  def countLazyValues(tree: global.DefDef): Int = tree.count {
    case term@(_: global.TermTree | _: global.SymTree) => term.isLazy
  }

  /**
   * CF7: Counts the number of additional parameter lists
   */
  def countParameterLists(tree: global.DefDef): Int = {
    val size = tree.vparamss.size
    if (size > 1) size - 1 else 0
  }

  /**
   * CO1: Counts the number of variable usage
   */
  def countVariables(tree: global.DefDef): Int = tree.count {
    case term@(_: global.TermTree | _: global.SymTree) => term.isVar
  }

  /**
   * CO1a: Counts the number of variable definitions
   */
  def countVariableDefinitions(tree: global.DefDef): Int = tree.count {
    case tree: global.ValDef => tree.isVar
  }

  /**
   * CO1b: Counts the assignesnments to inner variables
   */
  def countInnerVariableAssignment(tree: global.DefDef): Int = tree.count {
    case _: global.Assign => true // Inner variable assign
  }

  /**
   * CO1c: Counts outer variable references
   */
  def countOuterVariableUsage(tree: global.DefDef): Int = tree.count {
    case tree: global.Select => tree.isVar
  }

  /**
   * CO1d: Counts outer variable assignments
   */
  def countOuterVariableAssignment(tree: global.DefDef): Int = tree.count {
    case tree: global.Select => tree.name.endsWith("_$eq") // Outer variable assign
  }

  /**
   * CO2: Counts the number of calls resulting in Unit
   */
  def countSideEffects(tree: global.DefDef): Int = tree.count {
    case tree@(_: global.TermTree | _: global.SymTree) => tree.isUnit
  }

  /**
   * BO2a: Checks whether the tree returns Unit
   */
  def isSideEffect(tree: global.DefDef): Int = tree.isUnit.toInt

  /**
   * CO2b: Count the number of calls resulting in Unit
   */
  def countSideEffectCalls(tree: global.DefDef): Int = tree.count {
    case apply: global.Apply => apply.isUnit
    case _: global.Assign => true
  }

  /**
   * CO2c: Count the number of functions resulting in Unit
   */
  def countSideEffectFunctions(tree: global.DefDef): Int = tree.count {
    case function: global.Function => function.body.isUnit
  }

  override def run(arg: Global#DefDef): List[MetricResult] = {
    val tree = arg.asInstanceOf[global.DefDef]
    val f1 = countRecursiveCalls(tree)
    val f2 = countNestedDepth(tree)
    val f3 = countNestedMethods(tree)
    val f4 = countFunctions(tree)
    val f4a = isFunction(tree)
    val f4b = countFunctionParameters(tree)
    val f4c = countHigherOrderCalls(tree)
    val f4d = countFunctionCalls(tree)
    val f4e = countCurrying(tree)
    val f5 = countPatternMatching(tree)
    val f6 = countLazyValues(tree)
    val f7 = countParameterLists(tree)

    val fScore = f1 + f2 + f3 + f4 + f4a + f4b + f4c + f4d + f4e + f5 + f6 + f7

    val o1 = countVariables(tree)
    val o1a = countVariableDefinitions(tree)
    val o1b = countInnerVariableAssignment(tree)
    val o1c = countOuterVariableUsage(tree)
    val o1d = countOuterVariableAssignment(tree)
    val o2 = countSideEffects(tree)
    val o2a = isSideEffect(tree)
    val o2b = countSideEffectCalls(tree)
    val o2c = countSideEffectFunctions(tree)

    val oScore = o1 + o1a + o1b + o1c + o1d + o2 + o2c + o2b + o2a

    val score = (fScore - oScore) \ (fScore + oScore)

    List(
      MetricResult("CountRecursiveCalls", f1),
      MetricResult("CountNestedDepth", f2),
      MetricResult("CountNestedMethods", f3),
      MetricResult("CountFunctions", f4),
      MetricResult("CountFunctionParameters", f4b),
      MetricResult("CountHigherOrderCalls", f4c),
      MetricResult("CountFunctionCalls", f4d),
      MetricResult("CountCurrying", f4e),
      MetricResult("CountPatternMatching", f5),
      MetricResult("CountLazyValues", f6),
      MetricResult("CountParameterLists", f7),

      MetricResult("FunctionalScoreCount", fScore),

      MetricResult("CountVariables", o1),
      MetricResult("CountVariableDefinitions", o1a),
      MetricResult("CountInnerVariableAssignment", o1b),
      MetricResult("CountOuterVariableUsage", o1c),
      MetricResult("CountOuterVariableAssignment", o1d),

      MetricResult("CountSideEffects", o2),
      MetricResult("CountSideEffectCalls", o2b),
      MetricResult("CountSideEffectFunctions", o2c),

      MetricResult("ImperativeScoreCount", oScore),

      MetricResult("ParadigmScoreCount", score),
    )
  }
}
