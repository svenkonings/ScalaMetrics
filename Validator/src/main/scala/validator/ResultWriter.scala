package validator

import java.io.File
import java.nio.file.Files

import codeAnalysis.analyser.metric.{MetricResult, Result}
import codeAnalysis.util.Extensions.{DoubleExtension, ListExtension}

object ResultWriter {

  def writeAllMetrics(dir: File, methodology: String, results: List[Result], valueSep: String = ",", lineSep: String = "\n"): Unit = {
    val methodResults = results.flatMap(_.allMethods)
    writeMethodMetrics(dir, "method", methodology, methodResults, valueSep, lineSep)

    val objectResults = results.flatMap(_.allObjects)
    writeObjectMetrics(dir, "object", methodology, objectResults, valueSep, lineSep)
  }

  def writeMethodMetrics(dir: File, metricKind: String, methodology: String, methods: List[Result], valueSep: String, lineSep: String): Unit = {
    val fields = metricFields(methods.headOption)
    if (fields.nonEmpty) {
      val header = csvHeader(fields)
      val toCsv = resultToCsv(valueSep)(_)
      val body = methods.map(toCsv)
      val name = s"${metricKind}Results${methodology}"

      writeCsv(header, body, dir, name, valueSep, lineSep)
    }
  }

  def writeObjectMetrics(dir: File, metricKind: String, methodology: String, objects: List[Result], valueSep: String, lineSep: String): Unit = {
    val objectFields = metricFields(objects.headOption)
    val methodFields = metricFields(objects.flatMap(_.allMethods).headOption).filterNot(objectFields.contains)
    val fields = objectFields ::: methodFields
    if (fields.nonEmpty) {
      val results = if (objectFields.isEmpty) objects.filter(_.methods.nonEmpty) else objects
      val header = csvHeader(fields)
      val toCsv = resultToCsv(valueSep)(_)
      val objectBody = results.map(toCsv)
      if (methodFields.nonEmpty) {
        val toAvrCsv = resultAvrToCsv(methodFields, valueSep)(_, _)
        var avrBody = results.map(result => toAvrCsv(result, result.methods))
        avrBody = objectBody.zipWith(avrBody)(_ ::: _)
        val avrName = s"${metricKind}AvrResults${methodology}"
        writeCsv(header, avrBody, dir, avrName, valueSep, lineSep)

        val toSumCsv = resultSumToCsv(methodFields, valueSep)(_, _)
        var sumBody = results.map(result => toSumCsv(result, result.methods))
        sumBody = objectBody.zipWith(sumBody)(_ ::: _)
        val sumName = s"${metricKind}SumResults${methodology}"
        writeCsv(header, sumBody, dir, sumName, valueSep, lineSep)

        val toMaxCsv = resultMaxToCsv(methodFields, valueSep)(_, _)
        var maxBody = results.map(result => toMaxCsv(result, result.methods))
        maxBody = objectBody.zipWith(maxBody)(_ ::: _)
        val maxName = s"${metricKind}MaxResults${methodology}"
        writeCsv(header, maxBody, dir, maxName, valueSep, lineSep)
      } else {
        val name = s"${metricKind}Results${methodology}"
        writeCsv(header, objectBody, dir, name, valueSep, lineSep)
      }
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
    val maxs = childFields.map { field => childMetrics(field).map(_.value).maxOption.getOrElse(0.0) }
    csvRow(result, valueSep, maxs)
  }

  private def writeCsv(header: List[String], body: List[List[String]], dir: File, name: String, valueSep: String, lineSep: String): Unit = {
    val content = header :: body
    val csv = content.map(_.mkString(valueSep)).mkString(lineSep)
    dir.mkdirs()
    Files.write(new File(dir, s"$name.csv").toPath, csv.getBytes)
  }

  private def metricsByName(results: List[Result]): Map[String, List[MetricResult]] = results
    .flatMap(_.metrics)
    .groupBy(_.name)

  private def csvRow(result: Result, valueSep: String, values: List[Double]): List[String] =
    "HEAD" :: result.faults.toString :: getName(result, valueSep) :: values.map(_.toString)

  private def getName(result: Result, valueSep: String) = result.name.replace(valueSep, " ")
}
