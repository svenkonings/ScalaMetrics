package codeAnalysis.metrics.baseline

import codeAnalysis.analyser.Global
import codeAnalysis.analyser.metric._
import codeAnalysis.util.Extensions.DoubleExtension

object LinesOfCode extends MetricProducer {
  override def apply(global: Global): Metric = new LinesOfCode(global)
}

class LinesOfCode(val global: Global) extends FileMetric with ObjectMetric with MethodMetric {

  import global.TreeExtensions

  def runMetrics(arg: Global#Tree): List[MetricResult] = {
    val tree = arg.asInstanceOf[global.Tree]
    val pos = tree.pos
    val source = pos.source
    val startLine = source.offsetToLine(pos.start)
    val endLine = source.offsetToLine(pos.end)

    val emptyLineRegex = """^\s*$""".r
    val commentLineRegex = """.*(/\*[\s\S]*?\*/|//).*""".r

    val lines = source.lines(startLine, endLine + 1)
      .filterNot(emptyLineRegex.matches)
      .toList
    val lineCount = lines.size
    val codeLineCount = tree.lines(_ => true)
    val commentLineCount = commentLineRegex
      .findAllIn(lines.mkString("\n"))
      .flatMap(_.split('\n'))
      .size
    List(
      MetricResult("LOC", lineCount),
      MetricResult("SLOC", codeLineCount),
      MetricResult("CLOC", commentLineCount),
      MetricResult("CD", commentLineCount \ lineCount)
    )
  }

  override def run(tree: Global#PackageDef): List[MetricResult] =
    runMetrics(tree)

  override def run(tree: Global#ImplDef): List[MetricResult] =
    runMetrics(tree)

  override def run(tree: Global#DefDef): List[MetricResult] =
    runMetrics(tree)
}
