package validator

import codeAnalysis.metrics.baseline._
import codeAnalysis.metrics.paradigmScore.ParadigmScoreFraction

class BaselineValidatorTest extends UnitSpec("baseline", List(
  CouplingBetweenObjects,
  CyclomaticComplexity,
  DepthOfInheritance,
  DepthOfNesting,
  LackOfCohesionInMethods,
  LinesOfCode,
  NumberOfChildren,
  OutDegree,
  PatternSize,
  ResponseForClass,
  WeightedMethodCount,
  ParadigmScoreFraction
))
