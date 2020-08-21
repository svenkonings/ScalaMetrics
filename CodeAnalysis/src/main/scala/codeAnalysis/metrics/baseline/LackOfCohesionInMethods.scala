package codeAnalysis.metrics.baseline

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{Metric, MetricProducer, MetricResult, ObjectMetric}
import codeAnalysis.util.Extensions.DoubleExtension

import scala.collection.mutable

object LackOfCohesionInMethods extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new LackOfCohesionInMethods(compiler)
}

// See https://www.aivosto.com/project/help/pm-oo-cohesion.html#LCOM4
class LackOfCohesionInMethods(override val compiler: Compiler) extends ObjectMetric {

  import global.{SymbolExtensions, TreeExtensions}

  def LCOM1(tree: global.ImplDef): Double = {
    // LCOM Chidamber & Kemerer
    val fields = tree.myCollect { case valDef: global.ValDef if valDef.symbol.owner == tree.symbol => valDef.symbol.toString }
    val methods = tree.myCollect { case defDef: global.DefDef if defDef.symbol.owner == tree.symbol => defDef }

    val f = fields.size
    val m = methods.size
    val mf = fields.map(field => methods.count(_.myExists { case select: global.Select => select.symbol.toString == field })).sum
    val lcom = 1 - (mf \ (m * f))
    lcom
  }

  def LCOM2(tree: global.ImplDef): Double = {
    // LCOM Henderson-Sellers
    val fields = tree.myCollect { case valDef: global.ValDef if valDef.symbol.owner == tree.symbol => valDef.symbol.pathString }
    val methods = tree.myCollect { case defDef: global.DefDef if defDef.symbol.owner == tree.symbol => defDef }

    val f = fields.size
    val m = methods.size
    val mf = fields.map(field => methods.count(_.myExists { case select: global.Select => select.symbol.pathString == field })).sum
    val lcomhs = (m - (mf \ f)) \ (m - 1)
    if (lcomhs == -0.0) 0.0 else lcomhs
  }

  def LCOM4(tree: global.ImplDef): Double = {
    // LCOM Hitz & Montazeri
    case class Component(
      methods: mutable.Set[String] = mutable.Set(),
      values: mutable.Set[String] = mutable.Set(),
      calls: mutable.Set[String] = mutable.Set()
    )
    val components = mutable.ListBuffer[Component]()
    val methods = tree.myCollect { case defDef: global.DefDef if defDef.symbol.owner == tree.symbol => defDef }
    methods.foreach { method =>
      val name = method.symbol.pathString
      val values = mutable.Set.from(method.myCollect { case select: global.Select if select.symbol.owner == method.symbol.owner && select.isValOrVar => select.symbol.pathString })
      val calls = mutable.Set.from(method.myCollect { case select: global.Select if select.symbol.owner == method.symbol.owner && select.isMethod => select.symbol.pathString })
      // Methods a and b are related if they access the same class level variable or one calls the other
      val cohesiveComponent = components.find(
        component => values.exists(component.values) ||
        component.calls.contains(name) ||
        calls.exists(component.methods)
      )
      if (cohesiveComponent.isDefined) {
        val component = cohesiveComponent.get
        component.methods += name
        component.values ++= values
        component.calls ++= calls
      } else {
        val component = Component(mutable.Set(name), values, calls)
        components += component
      }
    }
    components.size
  }

  override def run(tree: global.ImplDef): List[MetricResult] =
    List(MetricResult("LackOfCohesionInMethods", LCOM4(tree)))
}
