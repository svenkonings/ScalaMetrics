package codeAnalysis.analyser

import codeAnalysis.{UnitSpec, analyser}
import codeAnalysis.analyser.Compiler.global._

class CompilerTest extends UnitSpec {
  test("Compile single file") {
    val tree = analyser.Compiler.treeFromFile(resources + "analyser/Simple.scala")
    assert(tree.isInstanceOf[Tree])
  }

  test("Compile same file") {
    val tree1 = analyser.Compiler.treeFromFile(resources + "analyser/Simple.scala")
    val tree2 = analyser.Compiler.treeFromFile(resources + "analyser/Simple.scala")
    assert(tree1 == tree2)
  }
}
