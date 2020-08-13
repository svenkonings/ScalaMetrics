package codeAnalysis.analyser.metric

import codeAnalysis.analyser.{Compiler, Global}

class MetricRunner(val metricProducers: List[MetricProducer])(val compiler: Compiler) {
  val global: Global = compiler.global
  val metrics: List[Metric] = metricProducers.map(_ (compiler))
  val fileMetrics: List[FileMetric] = metrics.collect { case m: FileMetric => m }
  val objectMetrics: List[ObjectMetric] = metrics.collect { case m: ObjectMetric => m }
  val methodMetrics: List[MethodMetric] = metrics.collect { case m: MethodMetric => m }

  val traverser: global.ParentTraverser[Result] = new global.ParentTraverser(parent => {
    case tree: global.PackageDef => runFileMetrics(parent, tree)
    case tree: global.ImplDef => runObjectMetrics(parent, tree)
    case tree: global.DefDef => runMethodMetrics(parent, tree)
  })

  private def getName(parent: Option[Result], tree: Global#Tree): String = {
    val name = tree.symbol.toString
    parent match {
      case Some(parent) => parent.name + " - " + name
      case None => name
    }
  }

  private def getInfo(parent: Option[Result], tree: Global#Tree): (String, String, Int, Int) = {
    val name = getName(parent, tree)
    val pos = tree.pos
    val source = pos.source
    val path = source.file.canonicalPath
    val startLine = source.offsetToLine(pos.start)
    val endLine = source.offsetToLine(pos.end)
    (name, path, startLine, endLine)
  }

  private def addResults[T <: Result](result: T, parent: Option[Result], metricResults: List[MetricResult]): T = {
    result.addMetrics(metricResults)
    parent.foreach(_.addResult(result))
    result
  }

  def runFileMetrics(parent: Option[Result], tree: Global#PackageDef): FileResult = {
    val result = FileResult.tupled(getInfo(parent, tree))
    val metricResults = fileMetrics.flatMap(_ (tree))
    addResults(result, parent, metricResults)
  }

  def runObjectMetrics(parent: Option[Result], tree: Global#ImplDef): ObjectResult = {
    val result = ObjectResult.tupled(getInfo(parent, tree))
    val metricResults = objectMetrics.flatMap(_ (tree))
    addResults(result, parent, metricResults)
  }

  def runMethodMetrics(parent: Option[Result], tree: Global#DefDef): MethodResult = {
    val result = MethodResult.tupled(getInfo(parent, tree))
    val metricResults = methodMetrics.flatMap(_ (tree))
    addResults(result, parent, metricResults)
  }

  def run(tree: Global#Tree): Option[Result] = traverser.top(tree)

  def runAll(trees: List[Global#Tree]): List[Result] = trees.map(run).collect { case Some(result) => result }
}
