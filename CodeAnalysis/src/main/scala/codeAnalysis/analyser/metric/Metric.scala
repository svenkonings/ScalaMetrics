package codeAnalysis.analyser.metric

import codeAnalysis.analyser.Global

trait MetricProducer {
  def apply(implicit global: Global): Metric
}

trait Metric {
  val global: Global
}

trait FileMetric extends Metric {
  def run(tree: Global#PackageDef): List[MetricResult]
}

trait ObjectMetric extends Metric {
  def run(tree: Global#ImplDef): List[MetricResult]
}

trait MethodMetric extends Metric {
  def run(tree: Global#DefDef): List[MetricResult]
}
