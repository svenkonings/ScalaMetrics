package codeAnalysis.analyser

import java.io.File

import codeAnalysis.analyser.metric.{Metric, MetricRunner, Result}

class Analyser(path: String, metrics: List[Metric], includeTest: Boolean = false) {
  val projectFiles: List[File] = getProjectFiles(path)
  val runner = new MetricRunner(metrics)

  private def getProjectFiles(path: String): List[File] = {
    def isTestPath(dir: File) = dir.getPath.contains(File.separator + "test" + File.separator)

    def isScalaFile(filename: String) = filename.endsWith(".scala")

    val file = new File(path)
    if (file.isFile)
      List(file)
    else
      file.listFiles((dir, name) => (includeTest || !isTestPath(dir)) && isScalaFile(name)).toList
  }

  def analyse(): List[Result] = runner.runFiles(projectFiles)
}
