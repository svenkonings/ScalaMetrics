package codeAnalysis.analyser.traverser

import codeAnalysis.analyser.Compiler.global._

object FractionPart extends Enumeration {
  type FractionPart = Value
  val Numerator, Denominator, Both, None = Value
}

import codeAnalysis.analyser.traverser.FractionPart._

class FractionTraverser(f: PartialFunction[Tree, FractionPart]) extends ScapegoatTraverser {
  private var numerator, denominator = 0

  def fraction(tree: Tree): Double = {
    numerator = 0
    denominator = 0
    traverse(tree)
    numerator.toDouble / denominator.toDouble
  }


  override protected def inspect(tree: Tree): Unit = {
    if (f.isDefinedAt(tree)) {
      f(tree) match {
        case Numerator => numerator += 1
        case Denominator => denominator += 1
        case Both => numerator += 1; denominator += 1
        case None =>
      }
    }
    continue(tree)
  }
}
