package codeAnalysis.metrics

import codeAnalysis.analyser.Global
import codeAnalysis.analyser.metric.{MethodMetric, Metric, MetricProducer, MetricResult}
import codeAnalysis.util.Extensions._

object ParadigmScoreLandkroon extends MetricProducer {
  override def apply(global: Global): Metric = new ParadigmScoreLandkroon(global)
}

class ParadigmScoreLandkroon(val global: Global) extends MethodMetric {

  import global.TreeExtensions

  private val functionalFuncs = List("foldLeft", "foldRight", "fold", "map", "filter", "count", "exist", "find")
  private val impFuncs = List("foreach")
  private val whileRegex = """^(w|doW)hile\$(\d)*""".r

  /**
   * Checks whether the method is recursive or not
   */
  def recursive(tree: global.DefDef): Int = tree.exists {
    case apply: global.Apply if tree.symbol == apply.symbol => true
    case _ => false
  }.toInt

  /**
   * Counts the number of variable usage
   */
  def variables(tree: global.DefDef): Int = tree.count(_.isVar)

  /**
   * Checks whether the method is nested or not
   */
  def nested(tree: global.DefDef): Int = tree.symbol.owner.isMethod.toInt

  /**
   * Counts the number of functional and imperative calls
   */
  def countFuncCalls(tree: global.Tree): (Int, Int) = tree.fold((0, 0))((previous, tree) => previous + (tree match {
    case tree: global.Apply if (tree.symbol != null && functionalFuncs.contains(tree.symbol.name.toString)) => (1, 0)
    case tree: global.Apply if (tree.symbol != null && impFuncs.contains(tree.symbol.name.toString)) => (0, 1)
    case _: global.Match => (1, 0)
    case tree: global.LabelDef if whileRegex matches tree.name.toString => (0, 1)
    case _ => (0, 0)
  }))

  /**
   * Counts the number of higher-order parameters
   */
  def higherOrderParams(tree: global.DefDef): Int = tree.vparamss.map(_.count(_.isFunction)).sum

  override def run(arg: Global#DefDef): List[MetricResult] = {
    val tree = arg.asInstanceOf[global.DefDef]
    val sideEffects = variables(tree)
    val isRecursive = recursive(tree)
    val isNested = nested(tree)
    val (func, imp) = countFuncCalls(tree)
    val higherOrderParamCount = higherOrderParams(tree)

    val funcPoints = isRecursive + isNested + func + higherOrderParamCount
    val impPoints = imp + sideEffects

    val paradigmScore = funcPoints \ (funcPoints + impPoints)
    List(
      MetricResult("FunctionalCallsLandkroon", func),
      MetricResult("ImperativeCallsLandkroon", imp),
      MetricResult("FunctionalPointsLandkroon", funcPoints),
      MetricResult("ImperativePointsLandkroon", impPoints),
      MetricResult("ParadigmScoreLandkroon", paradigmScore)
    )
  }
}
