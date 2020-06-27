package codeAnalysis.analyser.traverser

import codeAnalysis.analyser.Compiler.global._

import scala.collection.mutable

class ParentTraverser[T](f: Option[T] => PartialFunction[Tree, T]) extends ScapegoatTraverser {

  private val stack: mutable.Stack[T] = mutable.Stack()

  def top(tree: Tree): Option[T] = {
    stack.clear()
    traverse(tree)
    stack.headOption
  }

  override protected def inspect(tree: Tree): Unit = {
    val head = stack.headOption
    val partialFunc = f(head)
    if (partialFunc.isDefinedAt(tree)) {
      stack.push(partialFunc(tree))
      continue(tree)
      if (head.isDefined) stack.pop() // Keep top value for return
    } else {
      continue(tree)
    }
  }
}
