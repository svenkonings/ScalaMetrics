package codeAnalysis.metrics

import scala.language.implicitConversions

class ImplicitFile {

  implicit class ImplicitClass(value: ImplicitFile) {

  }

  class Test

  def useImplicitConversion(value: ImplicitFile): ImplicitClass = value

  implicit def implicitMethod(value: Test): ImplicitFile = value.asInstanceOf[ImplicitFile]

  implicit val implicitValue: Int = 3

  def useImplicitParameter(implicit value: Int): Int = value
  useImplicitParameter
}
