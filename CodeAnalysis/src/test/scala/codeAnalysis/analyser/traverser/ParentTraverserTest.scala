package codeAnalysis.analyser.traverser

import codeAnalysis.UnitSpec
import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{FileResult, MethodResult, ObjectResult, Result}

import scala.tools.nsc.interactive.Global

class ParentTraverserTest extends UnitSpec {

  def assertFileResult(result: Result, files: Int = 0, objects: Int = 0, methods: Int = 0): Unit = {
    assert(result.isInstanceOf[FileResult])
    assertChildren(result, files, objects, methods)
  }

  def assertObjectResult(result: Result, files: Int = 0, objects: Int = 0, methods: Int = 0): Unit = {
    assert(result.isInstanceOf[ObjectResult])
    assertChildren(result, files, objects, methods)
  }

  def assertMethodResult(result: Result, files: Int = 0, objects: Int = 0, methods: Int = 0): Unit = {
    assert(result.isInstanceOf[MethodResult])
    assertChildren(result, files, objects, methods)
  }

  def assertChildren(result: Result, files: Int, objects: Int, methods: Int): Unit = {
    assertResult(result.files.size)(files)
    assertResult(result.objects.size)(objects)
    assertResult(result.methods.size)(methods)
  }

  test("Tree traverser test") {
    val source = Compiler.fileToSource(resources + "analyser/traverser/ParentTraverserTree.scala")
    val compiler = new Compiler()
    val tree = compiler.treeFromSource(source)
    val traverser = new compiler.global.ParentTraverser[Result](parent => {
      case t: compiler.global.PackageDef =>
        val result = FileResult("", "", 0 ,0)
        parent.foreach(_.addResult(result))
        result
      case t: compiler.global.ImplDef =>
        val result = ObjectResult("", "", 0 ,0)
        parent.foreach(_.addResult(result))
        result
      case t: compiler.global.DefDef =>
        val result = MethodResult("", "", 0 ,0)
        parent.foreach(_.addResult(result))
        result
    })
    val result = traverser.top(tree)
    assert(result.isDefined)
    val fileResult = result.get
    assertFileResult(fileResult, objects = 1)
    val objectResult = fileResult.objects.head
    assertObjectResult(objectResult, objects = 2, methods = 1)
    assertMethodResult(objectResult.methods.head)
    val List(firstObject, secondObject) = objectResult.objects
    assertObjectResult(firstObject, objects = 1, methods = 1)
    assertObjectResult(firstObject.objects.head)
    assertMethodResult(firstObject.methods.head)
    assertObjectResult(secondObject)
  }
}
