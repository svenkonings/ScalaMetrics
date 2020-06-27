package codeAnalysis.analyser.metric

import codeAnalysis.analyser.Compiler.global._

trait Metric {}

trait FileMetric extends Metric {
  def run(tree: PackageDef): List[MetricResult]
}

trait ObjectMetric extends Metric {
  def run(tree: ImplDef): List[MetricResult]
}

trait MethodMetric extends Metric {
  def run(tree: DefDef): List[MetricResult]
}
