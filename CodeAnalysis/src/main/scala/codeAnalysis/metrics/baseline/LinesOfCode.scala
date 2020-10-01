package codeAnalysis.metrics.baseline

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric._
import codeAnalysis.util.Extensions.DoubleExtension

object LinesOfCode extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new LinesOfCode(compiler)
}

class LinesOfCode(override val compiler: Compiler) extends FileMetric with ObjectMetric with MethodMetric {

  import global.TreeExtensions

  def linesOfCode(tree: global.Tree): List[MetricResult] = {
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
    val codeLineCount = tree.linesTraverse(_ => true)
    val commentLineCount = commentLineRegex
      .findAllIn(lines.mkString("\n"))
      .flatMap(_.split('\n'))
      .size
    List(
      MetricResult("LinesOfCode", lineCount),
      MetricResult("SourceLinesOfCode", codeLineCount),
      MetricResult("CommentLinesOfCode", commentLineCount),
      MetricResult("CommentDensity", commentLineCount \ (commentLineCount + codeLineCount))
    )
  }

  override def run(tree: global.PackageDef): List[MetricResult] =
    linesOfCode(tree)

  override def run(tree: global.ImplDef): List[MetricResult] =
    linesOfCode(tree)

  override def run(tree: global.DefDef): List[MetricResult] =
    linesOfCode(tree)
}
