package codeAnalysis.analyser.metric

/**
 * Contains the result value of a single metric.
 *
 * @param name  the name of the metric
 * @param value the value of the metric
 */
case class MetricResult(name: String, value: Double) {
  override def toString: String = s"$name: $value"

  def toString(indent: Int): String = " " * indent + toString
}
