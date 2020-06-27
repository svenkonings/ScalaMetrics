package codeAnalysis.analyser.traverser

class ParentTraverserTree {
  def myMethod: Unit = ???

  val myVal: Int = 0

  object MyObject {
    val myVal: Int = 1

    def myMethod: Unit = ???

    trait MyTrait {

    }

  }

  class MyClass {

  }

}
