package codeAnalysis.analyser.metric

import codeAnalysis.analyser.{Compiler, Global}

/**
 * Runner used to traverse compiler trees and gather metric results.
 * The metrics are instantiated using the specified metric producers and compiler instance.
 *
 * @param metricProducers the specified metric producers
 * @param compiler        the specified compiler instance
 */
class MetricRunner(val metricProducers: List[MetricProducer])(val compiler: Compiler) {
  /**
   * The global instance, which provides a set of types and operations associated with a compiler run.
   *
   * @see `scala.reflect.api.Universe`
   */
  val global: Global = compiler.global

  /**
   * The list of instantiated metrics.
   */
  val metrics: List[Metric] = metricProducers.map(_ (compiler))

  /**
   * The list of instantiated file metrics.
   */
  val fileMetrics: List[FileMetric] = metrics.collect { case m: FileMetric => m }

  /**
   * The list of instantiated object metrics.
   */
  val objectMetrics: List[ObjectMetric] = metrics.collect { case m: ObjectMetric => m }

  /**
   * The list of instantiated method metrics.
   */
  val methodMetrics: List[MethodMetric] = metrics.collect { case m: MethodMetric => m }

  /**
   * A traverser used to traverse compiler trees and run the associated metrics.
   */
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

  /**
   * Traverse the specified tree and gather the metric results.
   *
   * @param tree the specified tree
   * @return tree-like representation of the results, if any.
   */
  def run(tree: Global#Tree): Option[Result] = traverser.top(tree)

  /**
   * Traverse the specified list of trees and gather the metric results.
   *
   * @param trees the specified list of treed
   * @return list of the results, for each tree with results
   */
  def runAll(trees: List[Global#Tree]): List[Result] = trees.map(run).collect { case Some(result) => result }
}
