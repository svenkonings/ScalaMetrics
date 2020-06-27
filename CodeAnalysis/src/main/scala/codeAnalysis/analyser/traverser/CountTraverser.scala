package codeAnalysis.analyser.traverser

import codeAnalysis.analyser.Compiler.global._

class CountTraverser(f: PartialFunction[Tree, Boolean]) extends
  SumTraverser(f.andThen(result => if (result) 1 else 0)) {
  def count(tree: Tree): Int = sum(tree)
}
