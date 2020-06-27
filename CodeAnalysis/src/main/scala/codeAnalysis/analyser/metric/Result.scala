package codeAnalysis.analyser.metric

import codeAnalysis.analyser.Compiler.global._

import scala.collection.mutable.ListBuffer

abstract class Result {
  private val _results: ListBuffer[Result] = ListBuffer()
  private val _metrics: ListBuffer[MetricResult] = ListBuffer()

  def name: String

  def addResult(result: Result): Result = {
    _results += result
    this
  }

  def addResults(results: List[Result]): Result = {
    _results ++= results
    this
  }

  def addMetric(metric: MetricResult): Result = {
    _metrics += metric
    this
  }

  def addMetrics(metrics: List[MetricResult]): Result = {
    _metrics ++= metrics
    this
  }

  def results: List[Result] = _results.toList

  def allResults: List[Result] = results ::: results.flatMap(_.allResults)

  def metrics: List[MetricResult] = _metrics.toList

  def allMetrics: List[MetricResult] = metrics ::: results.flatMap(_.allMetrics)

  def methods: List[MethodResult] = results.collect {
    case result: MethodResult => result
  }

  def allMethods: List[MethodResult] = results.flatMap {
    case result: MethodResult => result :: result.allMethods
    case result => result.allMethods
  }

  def objects: List[ObjectResult] = results.collect {
    case result: ObjectResult => result
  }

  def allObjects: List[ObjectResult] = results.flatMap {
    case result: ObjectResult => result :: result.allObjects
    case result => result.allObjects
  }

  def files: List[FileResult] = results.collect {
    case result: FileResult => result
  }

  def allFiles: List[FileResult] = results.flatMap {
    case result: FileResult => result :: result.allFiles
    case result => result.allFiles
  }

  override def toString: String = toString(0)

  def toString(indent: Int): String =
    " " * indent + (name :: metrics.map(_.toString(indent + 2)) ::: results.map(_.toString(indent + 2))).mkString("\n")
}

case class FileResult(tree: PackageDef) extends Result {
  override def name: String = tree.symbol.toString
}

case class ObjectResult(tree: ImplDef) extends Result {
  override def name: String = tree.symbol.toString
}

case class MethodResult(tree: DefDef) extends Result {
  override def name: String = tree.symbol.toString
}
