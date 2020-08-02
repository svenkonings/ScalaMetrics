package validator

import java.io.File
import java.nio.file.Files

import codeAnalysis.analyser.metric.{MethodResult, MetricResult, ObjectResult, Result}
import codeAnalysis.util.Extensions.DoubleExtension

object ResultWriter {

  def writeMethodMetrics(dir: File, name: String, results: List[Result], valueSep: String = ",", lineSep: String = "\n"): Unit = {
    val fields = methodMetricFields(results)

    val header = csvHeader(fields)
    val methodResults = results.flatMap(_.allMethods)
    val toCsv = methodToCsv(fields)(_)
    val body = methodResults.map(toCsv)

    val content = header :: body
    val csv = content.map(_.mkString(valueSep)).mkString(lineSep)
    write(new File(dir, s"$name.csv"), csv)
  }

  def writeObjectMetrics(dir: File, name: String, results: List[Result], valueSep: String = ",", lineSep: String = "\n"): Unit = {
    // TODO: Also sum and max
    val fields = methodMetricFields(results)

    val header = csvHeader(fields)
    val objectResults = results.flatMap(_.allObjects).filter(_.methods.nonEmpty)
    val toCsv = objectToCsv(fields)(_)
    val body = objectResults.map(toCsv)

    val content = header :: body
    val csv = content.map(_.mkString(valueSep)).mkString(lineSep)
    write(new File(dir, s"$name.csv"), csv)
  }

  private def csvHeader(fields: List[String]): List[String] =
    "commit" :: "faults" :: "path" :: fields

  private def methodToCsv(fields: List[String])(result: MethodResult): List[String] =
    "HEAD" :: result.faults.toString :: result.name :: metricValues(fields)(result)

  private def metricValues(fields: List[String])(result: Result): List[String] = result.metrics
    .filter(metric => fields.contains(metric.name))
    .map(_.value.toString)

  private def objectToCsv(fields: List[String])(result: ObjectResult): List[String] = {
    val metrics = objectMetricsByName(result)
    val averages = fields.map(field => {
      val values = metrics(field).map(_.value)
      values.sum \ values.size
    })
    "HEAD" :: result.faults.toString :: result.name :: averages.map(_.toString)
  }

  private def objectMetricsByName(result: ObjectResult): Map[String, List[MetricResult]] = result.methods
    .flatMap(_.metrics)
    .groupBy(_.name)

  /**
   * Returns the names of all method metrics with values
   */
  def methodMetricFields(results: List[Result]): List[String] = {
    val methodFields = methodMetricNames(results)
    val zeroFields = zeroMetrics(results)
    println("Zero fields:", zeroFields)
    methodFields diff zeroFields
  }

  /**
   * Returns the names of all method metrics
   */
  private def methodMetricNames(results: List[Result]): List[String] = results
    .find(_.allMethods.nonEmpty)
    .get
    .allMethods
    .head
    .metrics
    .map(_.name)

  /**
   * Returns the names of metrics wich only have 0 as value
   */
  def zeroMetrics(results: List[Result]): List[String] = results
    .flatMap(_.allMetrics)
    .groupBy(_.name)
    .filter(_._2.forall(_.value == 0.0)) // All values are 0
    .keys
    .toList


  private def write(file: File, contents: String): Unit =
    Files.write(file.toPath, contents.getBytes())
}
