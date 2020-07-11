package codeAnalysis.analyser.metric

import codeAnalysis.analyser.Global

class MetricRunner(val metricProducers: List[MetricProducer])(implicit val global: Global) {
  val metrics: List[Metric] = metricProducers.map(_.apply(global))
  val fileMetrics: List[FileMetric] = metrics.collect { case m: FileMetric => m }
  val objectMetrics: List[ObjectMetric] = metrics.collect { case m: ObjectMetric => m }
  val methodMetrics: List[MethodMetric] = metrics.collect { case m: MethodMetric => m }

  val traverser: global.ParentTraverser[Result] = new global.ParentTraverser(parent => {
    case tree: global.PackageDef => runFileMetrics(parent, tree)
    case tree: global.ImplDef => runObjectMetrics(parent, tree)
    case tree: global.DefDef => runMethodMetrics(parent, tree)
  })

  private def addResults[T <: Result](result: T, parent: Option[Result], metricResults: List[MetricResult]): T = {
    result.addMetrics(metricResults)
    parent.foreach(_.addResult(result))
    result
  }

  def runFileMetrics(parent: Option[Result], tree: Global#PackageDef): FileResult = {
    val result = FileResult(tree)
    val metricResults = fileMetrics.flatMap(_.run(tree))
    addResults(result, parent, metricResults)
  }

  def runObjectMetrics(parent: Option[Result], tree: Global#ImplDef): ObjectResult = {
    val result = ObjectResult(tree)
    val metricResults = objectMetrics.flatMap(_.run(tree))
    addResults(result, parent, metricResults)
  }

  def runMethodMetrics(parent: Option[Result], tree: Global#DefDef): MethodResult = {
    val result = MethodResult(tree)
    val metricResults = methodMetrics.flatMap(_.run(tree))
    addResults(result, parent, metricResults)
  }

  def run(tree: Global#Tree): Option[Result] = traverser.top(tree)

  def runAll(trees: List[Global#Tree]): List[Result] = trees.map(run).collect { case Some(result) => result }
}
