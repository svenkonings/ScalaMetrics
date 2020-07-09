package codeAnalysis.analyser.metric

case class MetricResult(name: String, value: Double) {
  override def toString: String = s"$name: $value"

  def toString(indent: Int): String = " " * indent + toString
}
