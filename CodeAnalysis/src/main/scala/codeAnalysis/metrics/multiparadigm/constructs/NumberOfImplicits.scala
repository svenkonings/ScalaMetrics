package codeAnalysis.metrics.multiparadigm.constructs

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{Metric, MetricProducer, MetricResult, ObjectMetric}

import scala.reflect.internal.Flags

object NumberOfImplicits extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new NumberOfImplicits(compiler)
}

class NumberOfImplicits(override val compiler: Compiler) extends ObjectMetric {

  import global.TreeExtensions

  def implicitDefinitions(tree: global.Tree): Int = tree.countTraverse {
    case tree: global.MemberDef => tree.mods.hasFlag(Flags.IMPLICIT)
  }

  def implicitConversions(tree: global.Tree): Int = tree.countTraverse {
    case _: global.ApplyImplicitView => true
  }

  def implicitParameters(tree: global.Tree): Int = tree.countTraverse {
    case _: global.ApplyToImplicitArgs => true
  }

  override def run(tree: global.ImplDef): List[MetricResult] = List(
    MetricResult("ImplicitDefinitions", implicitDefinitions(tree)),
    MetricResult("ImplicitConversions", implicitConversions(tree)),
    MetricResult("ImplicitParameters", implicitParameters(tree))
  )
}
