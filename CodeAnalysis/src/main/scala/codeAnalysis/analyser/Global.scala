package codeAnalysis.analyser

import codeAnalysis.analyser.FractionPart.FractionPart
import codeAnalysis.util.Extensions.DoubleExtension

import scala.collection.mutable
import scala.tools.nsc.reporters.Reporter
import scala.tools.nsc.{Settings, interactive}

class Global(settings: Settings, reporter: Reporter) extends interactive.Global(settings, reporter) {

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
          if parents.collect { case x if x.tpe != null => x.tpe.typeSymbol.fullName }.contains("scala.reflect.api.TypeCreator") =>
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

  class ParentTraverser[T](f: Option[T] => PartialFunction[Tree, T]) extends ScapegoatTraverser {

    private val stack: mutable.Stack[T] = mutable.Stack()

    def top(tree: Global#Tree): Option[T] = {
      stack.clear()
      traverse(tree.asInstanceOf[Tree])
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

  class FoldTraverser[T](base: T)(f: (T, Tree) => T) extends ScapegoatTraverser {
    private var value: T = base

    def fold(tree: Global#Tree): T = {
      value = base
      traverse(tree.asInstanceOf[Tree])
      value
    }

    override protected def inspect(tree: Tree): Unit = {
      value = f(value, tree)
      continue(tree)
    }
  }

  class SumTraverser(f: PartialFunction[Tree, Int]) extends
    FoldTraverser[Int](0)((value, tree) => value + f.applyOrElse(tree, (_: Tree) => 0)) {
    def sum(tree: Global#Tree): Int = fold(tree)
  }

  class CountTraverser(f: PartialFunction[Tree, Boolean]) extends
    SumTraverser(f.andThen(result => if (result) 1 else 0)) {
    def count(tree: Global#Tree): Int = sum(tree)
  }

  class FractionTraverser(f: PartialFunction[Tree, FractionPart]) extends ScapegoatTraverser {
    private var numerator, denominator = 0

    def fraction(tree: Global#Tree): Double = {
      numerator = 0
      denominator = 0
      traverse(tree.asInstanceOf[Tree])
      numerator.toDouble \ denominator.toDouble
    }


    override protected def inspect(tree: Tree): Unit = {
      if (f.isDefinedAt(tree)) {
        f(tree) match {
          case FractionPart.Numerator => numerator += 1
          case FractionPart.Denominator => denominator += 1
          case FractionPart.Both => numerator += 1; denominator += 1
          case FractionPart.None =>
        }
      }
      continue(tree)
    }
  }

  implicit class TreeExtensions(tree: Tree) {
    def getTypeSymbol: Symbol = tree match {
      case tree: ValOrDefDef => tree.tpt.symbol
      case tree if tree.tpe != null => tree.tpe.typeSymbol
      case _ => null
    }

    def isFunction: Boolean = {
      def isFunctionSymbol(symbol: Symbol): Boolean =
        symbol.enclosingPackage.nameString.equals("scala") && symbol.nameString.startsWith("Function")

      val symbol = tree.getTypeSymbol
      symbol != null && (isFunctionSymbol(symbol) || symbol.parentSymbols.exists(isFunctionSymbol))
    }

    def isUnit: Boolean = {
      val symbol = tree.getTypeSymbol
      symbol != null && symbol.enclosingPackage.nameString.equals("scala") && symbol.nameString.equals("Unit")
    }

    def isLazy: Boolean = tree.symbol != null && tree.symbol.isLazy

    def isVar: Boolean = tree.symbol != null && tree.symbol.kindString.equals("variable")

    def contains(f: PartialFunction[Tree, Boolean]): Boolean = tree.exists(f.orElse(_ => false))

    def fold[T](base: T)(f: (T, Tree) => T): T = new FoldTraverser(base)(f).fold(tree)

    def sum(f: PartialFunction[Tree, Int]): Int = new SumTraverser(f).sum(tree)

    def count(f: PartialFunction[Tree, Boolean]): Int = new CountTraverser(f).count(tree)

    def parentTraverse[T](f: Option[T] => PartialFunction[Tree, T]): Option[T] = new ParentTraverser(f).top(tree)

    def fraction(f: PartialFunction[Tree, FractionPart]): Double = new FractionTraverser(f).fraction(tree)
  }

}
