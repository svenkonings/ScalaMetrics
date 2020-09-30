package validator

import codeAnalysis.metrics.multiparadigm.zuilhof._

class MultiparadigmZuilhofValidatorTest extends UnitSpec("multiparadigm-zuilhof", List(
  NumberOfLambdaFunctions,
  SourceLinesOfLambda,
  LambdaScore,
  LambdaFunctionsUsingVariables,
  LambdaFunctionsWithSideEffects
))
