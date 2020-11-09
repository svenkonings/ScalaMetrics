package codeAnalysis.analyser

import java.io.{Closeable, File}

import scala.reflect.internal.util.{BatchSourceFile, SourceFile}
import scala.reflect.io.AbstractFile
import scala.tools.nsc.Settings
import scala.tools.nsc.interactive.Response
import scala.tools.nsc.reporters.NoReporter
import scala.util.matching.Regex
import codeAnalysis.util.Extensions.SourceFileExtension

class Compiler extends Closeable {
  /**
   * The global instance, which provides a set of types and operations associated with this compiler.
   *
   * @see `scala.reflect.api.Universe`
   */
  val global: Global = {
    val settings = new Settings
    settings.usejavacp.value = true
    val reporter = new NoReporter(settings)
    // val reporter = new ConsoleReporter(settings)
    new Global(settings, reporter)
  }
  var loadedSources: List[SourceFile] = List()

  /**
   * Compute the operation on the compiler thread
   */
  def ask[A](op: () => A): A = global.askForResponse(op).get match {
    case Left(value) => value
    case Right(ex) => throw ex
  }

  /**
   * Loads the source. Only one set of sources can be loaded at once.
   *
   * @param source the source to load
   */
  def loadSource(source: SourceFile): Unit = loadSources(List(source))

  /**
   * Loads the sources. Only one set of sources can be loaded at once.
   *
   * @param sources the list of sources to load
   */
  def loadSources(sources: List[SourceFile]): Unit = {
    val response = new Response[Unit]
    global.askReload(sources, response)
    response.get match {
      case Left(_) => loadedSources = sources
      case Right(ex) => throw ex
    }
  }

  /**
   * Returns the tree of a loaded source.
   * Do not use on unloaded sources.
   *
   * @param source the loaded source
   * @return the tree of the loaded source
   */
  def treeFromLoadedSource(source: SourceFile): global.Tree = {
    val response = new Response[global.Tree]
    global.askLoadedTyped(source, keepLoaded = true, response)
    response.get match {
      case Left(tree) => tree
      case Right(_) => null // Tree could not be parsed
    }
  }

  /**
   * Returns the trees of the list of loaded sources.
   * Do not use on unloaded sources.
   *
   * @param sources the list of source
   * @return the list of trees
   */
  def treesFromLoadedSources(sources: List[SourceFile]): List[global.Tree] =
    sources.map(treeFromLoadedSource).filter(_ != null)

  /**
   * Returns the trees of loaded sources after they have been filtered.
   *
   * @param filter the regex filter
   * @return the list of trees
   */
  def treesFromFilteredSources(filter: Regex): List[global.Tree] =
    treesFromLoadedSources(loadedSources.filter(source => filter.findFirstIn(source.text).isDefined))

  /**
   * Loads the source and returns the resulting tree.
   * Only one set of sources can be loaded at once.
   *
   * @param source the source to load
   * @return the resulting tree
   */
  def treeFromSource(source: SourceFile): global.Tree = {
    loadSource(source)
    treeFromLoadedSource(source)
  }

  /**
   * Loads the sources and returns the resulting trees.
   * Only one set of sources can be loaded at once.
   *
   * @param sources the list of sources to load
   * @return the list of resulting trees
   */
  def treesFromSources(sources: List[SourceFile]): List[global.Tree] = {
    loadSources(sources)
    treesFromLoadedSources(sources)
  }

  override def close(): Unit = {
    global.askShutdown()
    global.close()
  }
}

object Compiler {
  def fileToSource(file: AbstractFile): SourceFile =
    new BatchSourceFile(file)

  def fileToSource(file: File): SourceFile =
    fileToSource(AbstractFile.getFile(file))

  def fileToSource(path: String): SourceFile =
    fileToSource(AbstractFile.getFile(path))

  def stringToSource(filename: String, contents: String): SourceFile =
    new BatchSourceFile(filename, contents.toCharArray)
}
