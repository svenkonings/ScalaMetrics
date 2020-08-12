package codeAnalysis.metrics.baseline

import codeAnalysis.analyser.Global
import codeAnalysis.analyser.metric.{FileMetric, Metric, MetricProducer, MetricResult}

object SourceLinesOfCode extends MetricProducer {
  override def apply(global: Global): Metric = new SourceLinesOfCode(global)
}

class SourceLinesOfCode(val global: Global) extends FileMetric {

  import global.TreeExtensions

  override def run(arg: Global#PackageDef): List[MetricResult] = {
    val tree = arg.asInstanceOf[global.PackageDef]
    val lines = tree.lines(_ => true)
    List(MetricResult("SLOC", lines))
  }
}
