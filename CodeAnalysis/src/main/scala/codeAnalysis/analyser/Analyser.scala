package codeAnalysis.analyser

import java.io.File

import codeAnalysis.analyser.metric.{MetricProducer, MetricRunner, Result}

import scala.reflect.internal.util.SourceFile
import scala.util.Using.resource

class Analyser(path: String, metrics: List[MetricProducer], includeTest: Boolean = false) {

  var sourceFiles: List[SourceFile] = getProjectFiles(path)
  var scalaFiles: List[SourceFile] = sourceFiles.filter(!_.isJava)

  def refresh(): Unit = {
    sourceFiles = getProjectFiles(path)
    scalaFiles = sourceFiles.filter(!_.isJava)
  }

  private def getProjectFiles(path: String): List[SourceFile] = {
    def isTestPath(dir: File) = dir.getPath.contains(File.separator + "test" + File.separator)

    def isSourceFile(filename: String) = filename.endsWith(".scala") || filename.endsWith(".java")

    val file = new File(path)
    if (file.isFile)
      List(Compiler.fileToSource(file))
    else
      file.listFiles((dir, name) => (includeTest || !isTestPath(dir)) && isSourceFile(name))
        .map(Compiler.fileToSource)
        .toList
  }

  def analyse(): List[Result] = {
    resource(new Compiler)(compiler => {
      import compiler.global
      val runner = new MetricRunner(metrics)
      compiler.loadSources(sourceFiles)
      runner.runAll(compiler.treesFromLoadedSources(scalaFiles))
    })
  }
}
