package codeAnalysis.analyser.traverser

import codeAnalysis.analyser.Compiler.global._

// Code based on Scapegoat (https://github.com/sksamuel/scapegoat)
trait ScapegoatTraverser extends Traverser {

  protected def continue(tree: Tree): Unit = super.traverse(tree)

  protected def inspect(tree: Tree): Unit

  override final def traverse(tree: Tree): Unit = {
    tree match {
      // ignore synthetic methods added
      case DefDef(_, _, _, _, _, _) if tree.symbol.isSynthetic ||
        // MODIFIED: Additional ignored methods
        !tree.symbol.isSourceMethod || tree.symbol.isConstructor || tree.symbol.isAccessor =>
      case ClassDef(_, _, _, Template(parents, _, _))
        if parents.collect{case x if x.tpe != null =>x.tpe.typeSymbol.fullName}.contains("scala.reflect.api.TypeCreator") =>
      case _ if analyzer.hasMacroExpansionAttachment(tree) => //skip macros as per http://bit.ly/2uS8BrU
      case _ => inspect(tree)
    }
  }

  protected def isArray(tree: Tree): Boolean = tree.tpe.typeSymbol.fullName == "scala.Array"

  protected def isTraversable(tree: Tree): Boolean = tree.tpe <:< typeOf[Traversable[Any]]

  protected def isSeq(t: Tree): Boolean = t.tpe <:< typeOf[Seq[Any]]

  protected def isIndexedSeq(t: Tree): Boolean = t.tpe <:< typeOf[IndexedSeq[Any]]

  protected def isSet(t: Tree, allowMutableSet: Boolean = true): Boolean = {
    t.tpe.widen.baseClasses.exists { c =>
      (allowMutableSet && c.fullName == "scala.collection.mutable.Set") || c.fullName == "scala.collection.immutable.Set"
    }
  }

  protected def isList(t: Tree): Boolean = t.tpe <:< typeOf[scala.collection.immutable.List[Any]]

  protected def isMap(tree: Tree): Boolean =
    tree.tpe.baseClasses.exists {
      _.fullName == "scala.collection.Map"
    }
}
