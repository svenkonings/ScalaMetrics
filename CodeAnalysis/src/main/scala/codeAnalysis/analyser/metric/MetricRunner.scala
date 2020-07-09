package codeAnalysis.analyser.metric

import codeAnalysis.analyser.Compiler.global._
import codeAnalysis.analyser.traverser.ParentTraverser

class MetricRunner(val metrics: List[Metric]) {
  val fileMetrics: List[FileMetric] = metrics.collect { case m: FileMetric => m }
  val objectMetrics: List[ObjectMetric] = metrics.collect { case m: ObjectMetric => m }
  val methodMetrics: List[MethodMetric] = metrics.collect { case m: MethodMetric => m }

  val traverser: ParentTraverser[Result] = new ParentTraverser(parent => {
    case tree: PackageDef => runFileMetrics(parent, tree)
    case tree: ImplDef => runObjectMetrics(parent, tree)
    case tree: DefDef => runMethodMetrics(parent, tree)
  })

  private def addResults[T <: Result](result: T, parent: Option[Result], metricResults: List[MetricResult]): T = {
    result.addMetrics(metricResults)
    parent.foreach(_.addResult(result))
    result
  }

  def runFileMetrics(parent: Option[Result], tree: PackageDef): FileResult = {
    val result = FileResult(tree)
    val metricResults = fileMetrics.flatMap(_.run(tree))
    addResults(result, parent, metricResults)
  }

  def runObjectMetrics(parent: Option[Result], tree: ImplDef): ObjectResult = {
    val result = ObjectResult(tree)
    val metricResults = objectMetrics.flatMap(_.run(tree))
    addResults(result, parent, metricResults)
  }

  def runMethodMetrics(parent: Option[Result], tree: DefDef): MethodResult = {
    val result = MethodResult(tree)
    val metricResults = methodMetrics.flatMap(_.run(tree))
    addResults(result, parent, metricResults)
  }

  def run(tree: Tree): Option[Result] = traverser.top(tree)

  def runAll(trees: List[Tree]): List[Result] = trees.map(run).collect { case Some(result) => result }
}
