package validator

import java.io.File
import java.nio.file.Files

import codeAnalysis.analyser.metric.{MetricResult, Result}
import codeAnalysis.util.Extensions.DoubleExtension

import scala.collection.immutable.SeqMap
import scala.collection.mutable

object ResultWriter {

  def writeAllMetrics(dir: File, name: String, results: List[Result], valueSep: String = ",", lineSep: String = "\n"): Unit = {
    writeMetrics(dir, "fileResults" + name, results, valueSep, lineSep)

    val objectResults = results.flatMap(_.allObjects)
    writeMetrics(dir, "objectResults" + name, objectResults, valueSep, lineSep)

    val methodResults = results.flatMap(_.allMethods)
    writeMetrics(dir, "methodResults" + name, methodResults, valueSep, lineSep)

    val objectMethodResults = SeqMap.from[Result, List[Result]](
      objectResults.map(result => result -> result.methods).filter(_._2.nonEmpty)
    )
    writeChildMetrics(dir, "objectMethodResults" + name, objectMethodResults, valueSep, lineSep)
  }

  def writeMetrics(dir: File, name: String, results: List[Result], valueSep: String, lineSep: String): Unit = {
    val fields = metricFields(results.headOption)
    if (fields.nonEmpty) {
      val header = csvHeader(fields)
      val toCsv = resultToCsv(valueSep)(_)
      val body = results.map(toCsv)

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

  def writeChildMetrics(dir: File, name: String, results: SeqMap[Result, List[Result]], valueSep: String = ",", lineSep: String = "\n"): Unit = {
    val childFields = metricFields(results.values.flatten.headOption)
    if (childFields.nonEmpty) {
      val fields = childFields.map(_ + "Avr") ::: childFields.map(_ + "Sum") ::: childFields.map(_ + "Max")

      val header = csvHeader(fields)
      val toCsv = resultChildrenToCsv(childFields, valueSep)(_, _)
      val body = results.map(toCsv.tupled).toList

      writeCsv(header, body, dir, name, valueSep, lineSep)
    }
  }

  private def resultChildrenToCsv(childFields: List[String], valueSep: String)(result: Result, children: List[Result]): List[String] = {
    val averages = mutable.ListBuffer[Double]()
    val sums = mutable.ListBuffer[Double]()
    val maximums = mutable.ListBuffer[Double]()
    val childMetrics = metricsByName(children)
    childFields.foreach { field =>
      val values = childMetrics(field).map(_.value)
      val sum = values.sum
      averages += sum \ values.size
      sums += values.sum
      maximums += values.max
    }
    csvRow(result, valueSep, averages.toList ::: sums.toList ::: maximums.toList)
  }

  private def metricsByName(results: List[Result]): Map[String, List[MetricResult]] = results
    .flatMap(_.metrics)
    .groupBy(_.name)
}
