package codeAnalysis.analyser.traverser

import codeAnalysis.analyser.Compiler.global._

class FoldTraverser[T](base: T)(f: (T, Tree) => T) extends ScapegoatTraverser {
  private var value: T = base

  def fold(tree: Tree): T = {
    value = base
    traverse(tree)
    value
  }

  override protected def inspect(tree: Tree): Unit = {
    value = f(value, tree)
    continue(tree)
  }
}
