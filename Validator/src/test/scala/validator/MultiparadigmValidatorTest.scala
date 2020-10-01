package validator

import codeAnalysis.metrics.multiparadigm.constructs._
import codeAnalysis.metrics.multiparadigm.zuilhof._

class MultiparadigmZuilhofValidatorTest extends UnitSpec("multiparadigm-zuilhof", List(
  NumberOfLambdaFunctions,
  SourceLinesOfLambda,
  LambdaScore,
  LambdaFunctionsUsingVariables,
  LambdaFunctionsWithSideEffects
))

class MultiparadigmConstructsValidatorTest extends UnitSpec("multiparadigm-constructs", List(
  NumberOfImplicits,
  OverridingPatternVariables,
  UsageOfNull,
  VariableTypes,
  NumberOfReturns
))