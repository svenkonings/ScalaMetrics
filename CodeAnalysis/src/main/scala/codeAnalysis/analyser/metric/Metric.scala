package codeAnalysis.analyser.metric

import codeAnalysis.analyser.{Compiler, Global}

trait MetricProducer {
  def apply(compiler: Compiler): Metric
}

trait Metric {
  val compiler: Compiler
  val global: Global = compiler.global
}

trait FileMetric extends Metric {
  def apply(tree: Global#PackageDef): List[MetricResult] =
    run(tree.asInstanceOf[global.PackageDef])

  def run(tree: global.PackageDef): List[MetricResult]
}

trait ObjectMetric extends Metric {
  def apply(tree: Global#ImplDef): List[MetricResult] =
    run(tree.asInstanceOf[global.ImplDef])

  def run(tree: global.ImplDef): List[MetricResult]
}

trait MethodMetric extends Metric {
  def apply(tree: Global#DefDef): List[MetricResult] =
    run(tree.asInstanceOf[global.DefDef])

  def run(tree: global.DefDef): List[MetricResult]
}
