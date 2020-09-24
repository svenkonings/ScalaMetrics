package validator

import codeAnalysis.metrics.multiparadigm._

class MultiparadigmValidatorTest extends UnitSpec("multiparadigm", List(
  NumberOfLambdaFunctions,
  SourceLinesOfLambda,
  LambdaScore,
  LambdaFunctionsUsingVariables,
  LambdaFunctionsWithSideEffects
))
