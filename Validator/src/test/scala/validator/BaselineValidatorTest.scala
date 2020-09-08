package validator

import codeAnalysis.metrics.baseline._

class BaselineValidatorTest extends UnitSpec("baseline", List(
  CouplingBetweenObjects,
  CyclomaticComplexity,
  DepthOfInheritance,
  LackOfCohesionInMethods,
  LinesOfCode,
  NumberOfChildren,
  OutDegree,
  PatternSize,
  ResponseForClass,
  WeightedMethodCount
))
