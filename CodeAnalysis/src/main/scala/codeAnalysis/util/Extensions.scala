package codeAnalysis.util

import codeAnalysis.analyser.Compiler.global._
import codeAnalysis.analyser.traverser.FractionPart.FractionPart
import codeAnalysis.analyser.traverser._

object Extensions {

  implicit class TreeExtensions(tree: Tree) {
    def getTypeSymbol: Symbol = tree match {
      case tree: ValOrDefDef => tree.tpt.symbol
      case tree => tree.tpe.typeSymbol
    }

    def isFunction: Boolean = {
      def isFunctionSymbol(symbol: Symbol): Boolean =
        symbol.enclosingPackage.nameString.equals("scala") && symbol.nameString.startsWith("Function")

      val symbol = tree.getTypeSymbol
      symbol != null && (isFunctionSymbol(symbol) || symbol.parentSymbols.exists(isFunctionSymbol))
    }

    def isUnit: Boolean = {
      val symbol = tree.getTypeSymbol
      symbol != null && symbol.enclosingPackage.nameString.equals("scala") && symbol.nameString.equals("Unit")
    }

    def isLazy: Boolean = tree.symbol != null && tree.symbol.isLazy

    def isVar: Boolean = tree.symbol != null && tree.symbol.kindString.equals("variable")

    def fold[T](base: T)(f: (T, Tree) => T): T = new FoldTraverser(base)(f).fold(tree)

    def sum(f: PartialFunction[Tree, Int]): Int = new SumTraverser(f).sum(tree)

    def count(f: PartialFunction[Tree, Boolean]): Int = new CountTraverser(f).count(tree)

    def parentTraverse[T](f: Option[T] => PartialFunction[Tree, T]): Option[T] = new ParentTraverser(f).top(tree)

    def fraction(f: PartialFunction[Tree, FractionPart]): Double = new FractionTraverser(f).fraction(tree)
  }

}
