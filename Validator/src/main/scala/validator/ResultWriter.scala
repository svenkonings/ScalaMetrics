package validator

import java.io.File
import java.nio.file.Files

import codeAnalysis.analyser.metric.{MethodResult, MetricResult, ObjectResult, Result}
import codeAnalysis.util.Extensions.DoubleExtension

import scala.collection.mutable

object ResultWriter {

  private def csvHeader(fields: List[String]): List[String] =
    "commit" :: "faults" :: "path" :: fields

  private def csvRow(result: Result, valueSep: String, values: List[Double]): List[String] =
    "HEAD" :: result.faults.toString :: getName(result, valueSep) :: values.map(_.toString)

  private def getName(result: Result, valueSep: String) = result.name.replace(valueSep, " ")

  def writeMethodMetrics(dir: File, name: String, results: List[Result], valueSep: String = ",", lineSep: String = "\n"): Unit = {
    val fields = methodMetricFields(results)

    val header = csvHeader(fields)
    val methodResults = results.flatMap(_.allMethods)
    val toCsv = methodToCsv(fields, valueSep)(_)
    val body = methodResults.map(toCsv)

    writeCsv(header, body, dir, name, valueSep, lineSep)
  }

  private def methodToCsv(fields: List[String], valueSep: String)(result: MethodResult): List[String] = {
    val values = result.metrics
      .filter(metric => fields.contains(metric.name))
      .map(_.value)
    csvRow(result, valueSep, values)
  }

  def writeObjectMetrics(dir: File, name: String, results: List[Result], valueSep: String = ",", lineSep: String = "\n"): Unit = {
    val fields = methodMetricFields(results)
    val objectFields = fields.map(_ + "Avr") ::: fields.map(_ + "Sum") ::: fields.map(_ + "Max")

    val header = csvHeader(objectFields)
    val objectResults = results.flatMap(_.allObjects).filter(_.methods.nonEmpty)
    val toCsv = objectToCsv(fields, valueSep)(_)
    val body = objectResults.map(toCsv)

    writeCsv(header, body, dir, name, valueSep, lineSep)
  }

  private def objectToCsv(fields: List[String], valueSep: String)(result: ObjectResult): List[String] = {
    val metrics = objectMetricsByName(result)
    val averages = mutable.ListBuffer[Double]()
    val sums = mutable.ListBuffer[Double]()
    val maximums = mutable.ListBuffer[Double]()
    fields.foreach(field => {
      val values = metrics(field).map(_.value)
      val sum = values.sum
      averages += sum \ values.size
      sums += sum
      maximums += values.max
    })
    csvRow(result, valueSep, averages.toList ::: sums.toList ::: maximums.toList)
  }

  /**
   * Returns the names of all method metrics with values
   */
  def methodMetricFields(results: List[Result]): List[String] = {
    val methodFields = methodMetricNames(results)
    val zeroFields = zeroMetrics(results)
    println(s"Metrics without non-zero values: ${zeroFields.mkString(", ")}")
    methodFields diff zeroFields
  }

  /**
   * Returns the names of all method metrics
   */
  def methodMetricNames(results: List[Result]): List[String] = results
    .collectFirst(_.allMethods match { case methods if methods.nonEmpty => methods.head })
    .get
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

  private def objectMetricsByName(result: ObjectResult): Map[String, List[MetricResult]] = result.methods
    .flatMap(_.metrics)
    .groupBy(_.name)

  private def writeCsv(header: List[String], body: List[List[String]], dir: File, name: String, valueSep: String, lineSep: String): Unit = {
    val content = header :: body
    val csv = content.map(_.mkString(valueSep)).mkString(lineSep)
    write(new File(dir, s"$name.csv"), csv)
  }

  private def write(file: File, contents: String): Unit =
    Files.write(file.toPath, contents.getBytes())
}
