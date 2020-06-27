package codeAnalysis.analyser.traverser

import codeAnalysis.analyser.Compiler.global._

class SumTraverser(f: PartialFunction[Tree, Int]) extends
  FoldTraverser[Int](0)((value, tree) => value + f.applyOrElse(tree, (_: Tree) => 0)) {
  def sum(tree: Tree): Int = fold(tree)
}
