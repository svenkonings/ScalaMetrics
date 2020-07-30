package codeAnalysis.analyser

import java.io.File

import codeAnalysis.analyser.metric.{MetricProducer, MetricRunner, Result}
import codeAnalysis.util.FileUtil._

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
    val file = new File(path)
    if (file.isFile)
      List(Compiler.fileToSource(file))
    else {
      def listFiles(file: File): List[SourceFile] =
        file.listFiles((dir, name) => (includeTest || !isTestPath(dir)) && (isSourceFile(name) || isDirectory(dir, name)))
          .flatMap {
            case file if file.isFile => List(Compiler.fileToSource(file))
            case dir if dir.isDirectory => listFiles(dir)
            case file => println("Not a file or directory: ", file); List()
          }
          .toList

      listFiles(file)
    }
  }

  def analyse(): List[Result] = resource(new Compiler) { compiler =>
    import compiler.global
    val runner = new MetricRunner(metrics)
    compiler.loadSources(sourceFiles)
    compiler.ask(() => runner.runAll(compiler.treesFromLoadedSources(scalaFiles)))
  }
}
