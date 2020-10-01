package codeAnalysis.metrics
import codeAnalysis.metrics.ImplicitFile
import scala.language.implicitConversions

class ImplicitImportFile {
  val other = new ImplicitFile()

  import other.ImplicitClass
  def useImplicitConversion(value: ImplicitFile): ImplicitClass = value
}
