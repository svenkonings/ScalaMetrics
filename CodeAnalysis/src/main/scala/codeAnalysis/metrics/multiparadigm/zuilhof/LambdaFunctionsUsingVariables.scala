package codeAnalysis.metrics.multiparadigm.zuilhof

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{Metric, MetricProducer, MetricResult, ObjectMetric}

object LambdaFunctionsUsingVariables extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new LambdaFunctionsUsingVariables(compiler)
}

class LambdaFunctionsUsingVariables(override val compiler: Compiler) extends ObjectMetric {

  import global.{SymbolExtensions, TreeExtensions}

  def functionsUsingOuterVariables(tree: global.ImplDef): Int = tree.countTraverse {
    case function: global.Function => function.existsTraverse {
      case select: global.Select => select.isVar
    }
  }

  def functionsUsingInnerVariables(tree: global.ImplDef): Int = tree.countTraverse {
    case function: global.Function => function.existsTraverse {
      case ident: global.Ident if !function.existsTraverse{
        case valDef: global.ValDef => valDef.symbol.qualifiedName == ident.symbol.qualifiedName
      } => ident.isVar
    }
  }

  override def run(tree: global.ImplDef): List[MetricResult] = List(
    MetricResult("LambdaFunctionsUsingOuterVariables", functionsUsingOuterVariables(tree)),
    MetricResult("LambdaFunctionsUsingInnerVariables", functionsUsingInnerVariables(tree))
  )
}
