package validator

import java.io.File
import java.nio.file.Files

import codeAnalysis.analyser.metric.{MethodResult, Metric, MetricResult, ObjectResult, Result}

import scala.annotation.tailrec

object ResultWriter {

  @tailrec
  private def methodMetricNames(result: Result): List[String] = result match {
    case result: MethodResult => result.metrics.map(_.name)
    case result => methodMetricNames(result.methods.head)
  }

  private def metricValues(result: Result): List[String] = result.metrics.map(_.value.toString)

  private def metricsByName(result: Result): Map[String, List[MetricResult]] = result.methods
    .flatMap(_.metrics)
    .groupBy(_.name)

  private def csvHeader(result: Result): List[String] =
    "commit" :: "faults" :: "path" :: methodMetricNames(result)

  private def methodToCsv(result: MethodResult): List[String] =
    "HEAD" :: result.faults.toString :: result.name :: metricValues(result)

  private def objectToCsv(result: ObjectResult): List[String] = {
    val fields = methodMetricNames(result)
    val metrics = metricsByName(result)
    val averages = fields.map(field => {
      val values = metrics(field).map(_.value)
      values.sum / values.size
    })
    "HEAD" :: result.faults.toString :: result.name :: averages.map(_.toString)
  }

  def writeMethodMetrics(dir: File, results: List[Result], valueSep: String = ",", lineSep: String = "\n"): Unit = {
    val functionResults = results.flatMap(_.allMethods)
    val csvContent = csvHeader(functionResults.head) :: functionResults.map(methodToCsv)
    val csv = csvContent.map(_.mkString(valueSep)).mkString(lineSep)
    write(new File(dir, "functionResults.csv"), csv)
  }

  def writeObjectMetrics(dir: File, results: List[Result], valueSep: String = ",", lineSep: String = "\n"): Unit = {
    val objectResults = results.flatMap(_.allObjects).filter(_.methods.nonEmpty)
    val csvContent = csvHeader(objectResults.head.methods.head) :: objectResults.map(objectToCsv)
    val csv = csvContent.map(_.mkString(valueSep)).mkString(lineSep)
    write(new File(dir, "objectResults.csv"), csv)
  }

  private def write(file: File, contents: String): Unit =
    Files.writeString(file.toPath, contents)
}
