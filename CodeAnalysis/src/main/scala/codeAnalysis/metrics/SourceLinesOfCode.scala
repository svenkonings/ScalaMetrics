package codeAnalysis.metrics

import codeAnalysis.analyser.Global
import codeAnalysis.analyser.metric.{FileMetric, Metric, MetricProducer, MetricResult}

object SourceLinesOfCode extends MetricProducer {
  override def apply(implicit global: Global): Metric = new SourceLinesOfCode
}

class SourceLinesOfCode(implicit val global: Global) extends FileMetric{
  import global.TreeExtensions

  override def run(tree: Global#PackageDef): List[MetricResult] = {
    val endLine = tree.pos.source.offsetToLine(tree.pos.end)
    List(MetricResult("SLOC", endLine))
  }
}
