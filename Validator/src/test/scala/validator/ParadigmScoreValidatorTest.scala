package validator

import codeAnalysis.metrics.paradigmScore._

class ParadigmScoreValidatorTest extends UnitSpec("paradigmScore", List(
  ParadigmScoreBool,
  ParadigmScoreCount,
  ParadigmScoreFraction,
  ParadigmScoreLandkroon
))

class ParadigmScoreBoolValidatorTest extends UnitSpec("paradigmScoreBool", List(ParadigmScoreBool))

class ParadigmScoreCountValidatorTest extends UnitSpec("paradigmScoreCount", List(ParadigmScoreCount))

class ParadigmScoreFractionValidatorTest extends UnitSpec("paradigmScoreFraction", List(ParadigmScoreFraction))

class ParadigmScoreLandkroonValidatorTest extends UnitSpec("paradigmScoreLandkroon", List(ParadigmScoreLandkroon))
