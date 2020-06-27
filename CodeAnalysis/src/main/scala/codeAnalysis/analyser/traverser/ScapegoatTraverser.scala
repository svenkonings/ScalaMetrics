package codeAnalysis.analyser.traverser

import codeAnalysis.analyser.Compiler.global._

// Code based on Scapegoat (https://github.com/sksamuel/scapegoat)
trait ScapegoatTraverser extends Traverser {

  private val SuppressWarnings = typeOf[SuppressWarnings]

  @scala.annotation.tailrec
  private def inspectionClass(klass: Class[_]): Class[_] =
    Option(klass.getEnclosingClass) match {
      case None => klass
      case Some(k) => inspectionClass(k)
    }

  private def isThisDisabled(an: AnnotationInfo): Boolean = {
    val cls = inspectionClass(getClass)
    val names = Set("all", cls.getSimpleName, cls.getCanonicalName).map(_.toLowerCase)
    val suppressedNames: Seq[String] = an.javaArgs.values.headOption.toSeq.flatMap {
      case ArrayAnnotArg(args) =>
        args.collect {
          case LiteralAnnotArg(Constant(suppressedName: String)) =>
            suppressedName.toLowerCase
        }
      case _ =>
        Seq.empty[String]
    }
    names.intersect(suppressedNames.toSet).nonEmpty
  }

  private def isSkipAnnotation(an: AnnotationInfo) =
  // Workaround for #222: we can't use typeOf[Safe] here it requires Scapegoat to be on the
  // compile classpath.
    an.tree.tpe =:= SuppressWarnings || an.tree.tpe.erasure.toString == "com.sksamuel.scapegoat.Safe"

  private def isSuppressed(symbol: Symbol) =
    symbol != null &&
      symbol.annotations.exists(an => isSkipAnnotation(an) && isThisDisabled(an))

  protected def continue(tree: Tree): Unit = super.traverse(tree)

  protected def inspect(tree: Tree): Unit

  override final def traverse(tree: Tree): Unit = {
    tree match {
      // ignore synthetic methods added
      case DefDef(_, _, _, _, _, _) if tree.symbol.isSynthetic ||
        // MODIFIED: Additional ignored methods
        !tree.symbol.isSourceMethod || tree.symbol.isConstructor || tree.symbol.isAccessor =>
      case member: MemberDef if isSuppressed(member.symbol) =>
      case block@Block(_, _) if isSuppressed(block.symbol) =>
      case iff@If(_, _, _) if isSuppressed(iff.symbol) =>
      case tri@Try(_, _, _) if isSuppressed(tri.symbol) =>
      case ClassDef(_, _, _, Template(parents, _, _))
        if parents.map(_.tpe.typeSymbol.fullName).contains("scala.reflect.api.TypeCreator") =>
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
