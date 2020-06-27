package codeAnalysis.analyser

import java.io.File

import scala.reflect.internal.util.{BatchSourceFile, SourceFile}
import scala.reflect.io.AbstractFile
import scala.tools.nsc.Settings
import scala.tools.nsc.interactive.Global
import scala.tools.nsc.reporters.ConsoleReporter

object Compiler {
  val global: Global = {
    val settings = new Settings
    settings.usejavacp.value = true
    val reporter = new ConsoleReporter(settings)
    new Global(settings, reporter)
  }

  import global._

  def treeFromFile(source: SourceFile): Tree = {
    val response = new Response[Tree]
    global.askLoadedTyped(source, keepLoaded = true, response)
    response.get match {
      case Left(tree) => tree
      case Right(ex) => throw ex
    }
  }

  def treeFromFile(file: AbstractFile): Tree = treeFromFile(new BatchSourceFile(file))

  def treeFromFile(file: File): Tree = treeFromFile(AbstractFile.getFile(file))

  def treeFromFile(path: String): Tree = treeFromFile(AbstractFile.getFile(path))
}
