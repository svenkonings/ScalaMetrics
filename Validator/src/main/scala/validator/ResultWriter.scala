package validator

import java.io.File
import java.nio.file.Files

import codeAnalysis.analyser.metric.{MetricResult, Result}
import codeAnalysis.util.Extensions.DoubleExtension

import scala.collection.immutable.SeqMap

object ResultWriter {

  def writeAllMetrics(dir: File, methodology: String, results: List[Result], valueSep: String = ",", lineSep: String = "\n"): Unit = {
    writeMetrics(dir, "file", methodology, results, valueSep, lineSep)

    val objectResults = results.flatMap(_.allObjects)
    writeMetrics(dir, "object", methodology, objectResults, valueSep, lineSep)

    val methodResults = results.flatMap(_.allMethods)
    writeMetrics(dir, "method", methodology, methodResults, valueSep, lineSep)

    val objectMethodResults = SeqMap.from[Result, List[Result]](
      objectResults.map(result => result -> result.methods).filter(_._2.nonEmpty)
    )
    writeChildMetrics(dir, "object", "method", methodology, objectMethodResults, valueSep, lineSep)
  }

  def writeMetrics(dir: File, metricKind: String, methodology: String, results: List[Result], valueSep: String, lineSep: String): Unit = {
    val fields = metricFields(results.headOption)
    if (fields.nonEmpty) {
      val header = csvHeader(fields)
      val toCsv = resultToCsv(valueSep)(_)
      val body = results.map(toCsv)
      val name = s"${metricKind}Results${methodology}"

      writeCsv(header, body, dir, name, valueSep, lineSep)
    }
  }

  private def metricFields(result: Option[Result]): List[String] =
    result.map(_.metrics.map(_.name)).getOrElse(List())

  private def csvHeader(fields: List[String]): List[String] =
    "commit" :: "faults" :: "path" :: fields

  private def resultToCsv(valueSep: String)(result: Result): List[String] = {
    val values = result.metrics.map(_.value)
    csvRow(result, valueSep, values)
  }

  private def writeCsv(header: List[String], body: List[List[String]], dir: File, name: String, valueSep: String, lineSep: String): Unit = {
    val content = header :: body
    val csv = content.map(_.mkString(valueSep)).mkString(lineSep)
    dir.mkdirs()
    Files.write(new File(dir, s"$name.csv").toPath, csv.getBytes)
  }

  private def csvRow(result: Result, valueSep: String, values: List[Double]): List[String] =
    "HEAD" :: result.faults.toString :: getName(result, valueSep) :: values.map(_.toString)

  private def getName(result: Result, valueSep: String) = result.name.replace(valueSep, " ")

  def writeChildMetrics(dir: File, parentKind: String, childKind: String, methodology: String, results: SeqMap[Result, List[Result]], valueSep: String, lineSep: String): Unit = {
    val metricKind = s"${parentKind}${childKind.capitalize}"
    val childFields = metricFields(results.values.flatten.headOption)
    if (childFields.nonEmpty) {
      val header = csvHeader(childFields)

      val toAvrCsv = (resultAvrToCsv(childFields, valueSep)(_, _)).tupled
      val avrBody = results.map(toAvrCsv).toList
      val avrName = s"${metricKind}AvrResults${methodology}"
      writeCsv(header, avrBody, dir, avrName, valueSep, lineSep)

      val toSumCsv = (resultSumToCsv(childFields, valueSep)(_, _)).tupled
      val sumBody = results.map(toSumCsv).toList
      val sumName = s"${metricKind}SumResults${methodology}"
      writeCsv(header, sumBody, dir, sumName, valueSep, lineSep)

      val toMaxCsv = (resultMaxToCsv(childFields, valueSep)(_, _)).tupled
      val maxBody = results.map(toMaxCsv).toList
      val maxName = s"${metricKind}MaxResults${methodology}"
      writeCsv(header, maxBody, dir, maxName, valueSep, lineSep)
    }
  }

  private def resultAvrToCsv(childFields: List[String], valueSep: String)(result: Result, children: List[Result]): List[String] = {
    val childMetrics = metricsByName(children)
    val averages = childFields.map { field =>
      val values = childMetrics(field).map(_.value)
      values.sum \ values.size
    }
    csvRow(result, valueSep, averages)
  }

  private def resultSumToCsv(childFields: List[String], valueSep: String)(result: Result, children: List[Result]): List[String] = {
    val childMetrics = metricsByName(children)
    val sums = childFields.map { field => childMetrics(field).map(_.value).sum }
    csvRow(result, valueSep, sums)
  }

  private def resultMaxToCsv(childFields: List[String], valueSep: String)(result: Result, children: List[Result]): List[String] = {
    val childMetrics = metricsByName(children)
    val maxs = childFields.map { field => childMetrics(field).map(_.value).max }
    csvRow(result, valueSep, maxs)
  }

  private def metricsByName(results: List[Result]): Map[String, List[MetricResult]] = results
    .flatMap(_.metrics)
    .groupBy(_.name)
}
