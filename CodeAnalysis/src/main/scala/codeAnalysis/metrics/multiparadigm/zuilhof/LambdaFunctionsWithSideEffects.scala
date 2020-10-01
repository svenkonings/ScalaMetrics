package codeAnalysis.metrics.multiparadigm.zuilhof

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{Metric, MetricProducer, MetricResult, ObjectMetric}

object LambdaFunctionsWithSideEffects extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new LambdaFunctionsWithSideEffects(compiler)
}

class LambdaFunctionsWithSideEffects(override val compiler: Compiler) extends ObjectMetric {

  import global.TreeExtensions

  def functionsWithSideEffects(tree: global.ImplDef): Int = tree.countTraverse {
    case tree: global.Function => tree.existsTraverse(_.isUnit)
  }

  def functionsWithAssignment(tree: global.ImplDef): Int = tree.countTraverse {
    case tree: global.Function => tree.existsTraverse {
      case _: global.Assign => true // Inner variable assign
      case tree: global.Select => tree.name.endsWith("_$eq") // Outer variable assign
    }
  }

  override def run(tree: global.ImplDef): List[MetricResult] = List(
    MetricResult("LambdaFunctionsWithSideEffects", functionsWithSideEffects(tree)),
    MetricResult("LambdaFunctionsWithAssignment", functionsWithAssignment(tree))
  )
}
