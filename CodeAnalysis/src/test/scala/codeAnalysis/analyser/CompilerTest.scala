package codeAnalysis.analyser

import codeAnalysis.UnitSpec

import scala.tools.nsc.interactive.Global
import scala.util.Using.resource

class CompilerTest extends UnitSpec {
  test("Compile single file") {
    resource(new Compiler) { compiler =>
      val source = Compiler.fileToSource(resources + "analyser/Simple.scala")
      val tree = compiler.treeFromSource(source)
      assert(tree.isInstanceOf[Global#Tree])
    }
  }

  test("Compile Java file") {
    resource(new Compiler) { compiler =>
      val source = Compiler.fileToSource(resources + "analyser/Simple.java")
      val tree = compiler.treeFromSource(source)
      assert(tree.isInstanceOf[Global#Tree])
    }
  }

  test("Compile same file") {
    resource(new Compiler) { compiler =>
      val source = Compiler.fileToSource(resources + "analyser/Simple.scala")
      val tree1 = compiler.treeFromLoadedSource(source)
      val tree2 = compiler.treeFromLoadedSource(source)
      assert(tree1 == tree2)
    }
  }

  test("Compile multiple files") {
    resource(new Compiler) { compiler =>
      val sources = List(
        resources + "analyser/test/Initial.scala",
        resources + "analyser/test/Correct.scala",
        resources + "analyser/test/Incorrect.scala"
      ).map(Compiler.fileToSource)
      compiler.loadSources(sources)
      val initial = compiler.treeFromLoadedSource(sources.head)
      val body = initial.asInstanceOf[Global#PackageDef].stats.head.asInstanceOf[Global#ClassDef].impl.body
      val correct = body(1).asInstanceOf[Global#DefDef]
      val incorrect = body(2).asInstanceOf[Global#DefDef]
      assert(correct.rhs.tpe.typeSymbol.nameString == "Int")
      assert(incorrect.rhs.tpe.typeSymbol.nameString == "<none>")
    }
  }

  test("Compile from String") {
    resource(new Compiler) { compiler =>
      val correct = Compiler.stringToSource("Correct",
        """
          |package test
          |
          |object Correct {
          |  def myMethod: Int = 5
          |}
        """.stripMargin)
      val incorrect = Compiler.stringToSource("Incorrect",
        """
          |package test
          |
          |object Incorrect {
          |  def myMethod: String = "Hello"
          |}
        """.stripMargin)
      val initial = Compiler.stringToSource("Initial",
        """
          |package test
          |
          |class Initial {
          |  def correct: Int = Correct.myMethod / 5
          |  def incorrect: Int = Incorrect.myMethod / 5
          |}
        """.stripMargin)
      val sources = List(correct, incorrect, initial)
      compiler.loadSources(sources)
      val tree = compiler.treeFromLoadedSource(initial)
      val body = tree.asInstanceOf[Global#PackageDef].stats.head.asInstanceOf[Global#ClassDef].impl.body
      val correctDef = body(1).asInstanceOf[Global#DefDef]
      val incorrectDef = body(2).asInstanceOf[Global#DefDef]
      assert(correctDef.rhs.tpe.typeSymbol.nameString == "Int")
      assert(incorrectDef.rhs.tpe.typeSymbol.nameString == "<none>")
    }
  }
}
