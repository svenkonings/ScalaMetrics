# ScalaMetrics
A metric analysis framework for Scala used to research multi-paradigm metrics.
This framework has been developed as part of a Master's thesis.

- Title: Source Code Metrics for Combined Functional and Object-Oriented
Programming in Scala
- Author: Sven Konings
- Year: 2020

Table of contents:
- [Paper abstract](#paper-abstract)
- [Framework design](#framework-design)
  - [GitClient](#gitclient)
  - [CodeAnalysis](#codeanalysis)
  - [Validator](#validator)
  - [ResultAnalysis](#resultanalysis)
- [Included data](#included-data)
- [Build requirements](#build-requirements)
- [Metric analysis process](#metric-analysis-process)
  - [Defining metrics](#defining-metrics)
  - [Measuring metrics](#measuring-metrics)
  - [Analysing metrics](#analysing-metrics)
  - [Plotting analysis results](#plotting-analysis-results)
- [Credits](#credits)

## Paper abstract
Source code metrics are used to measure and evaluate the code quality of
software projects. Metrics are available for both Object-Oriented Programming
(OOP) and Functional Programming (FP). However, there is little research on
source code metrics for the combination of OOP and FP. Furthermore, existing OOP
and FP metrics are not always applicable. For example, the usage of mutable
class variables (OOP) in lambda functions (FP) is a combination that does not
occur in either paradigm on their own. Existing OOP and FP metrics are therefore
unsuitable to give an indication of quality regarding these combined constructs.

Scala is a programming language which features an extensive combination of OOP
and FP construct. The goal of this thesis is to research metrics for Scala which
can detect potential faults when combining OOP and FP. We have implemented a
framework for defining and analysing Scala metrics. Using this framework we have
measured whether code was written using mostly OOP or FP-style constructs and
analysed whether this affected the occurrence of potential faults. Next we
implemented a baseline model of existing OOP and FP metrics. Candidate metrics
were added to this baseline model to verify whether they improve the fault
detection performance.

In the analysed projects, there was a relatively higher number of faults when
mixing OOP- and FP-style code. None of the researched candidate metrics
significantly improved the fault detection performance of the baseline model.
However, some of the metrics measured constructs for which over half of the
objects using those constructs contained faults.

## Framework design
![Framework design](https://github.com/svenkonings/ScalaMetrics/raw/master/img/Framework_design.svg?sanitize=true "Framework design")

### GitClient
The GitClient module is responsible for managing the Git project and its issues.
The module can retrieve all commits that refer to issues labelled as fault, it
can calculate the changes between two versions and it can retrieve all files of
a certain version of the code.

### CodeAnalysis
The CodeAnalysis module is responsible for analysing the code using metrics.
Given a set of files, it can parse the code, run the metrics and return the
results in a tree-like format based on the structure of the code. It contains
all the metrics and the utilities needed to define them.

### Validator
The Validator module is responsible for running the validation methodology
workflow (see paper). It uses the GitClient module to retrieve files for
analysis, getting the bugfix commits and getting the changes made by those
commits. Files for analysis are passed to the CodeAnalysis module, which returns
the results back to the Validator module. The results are then processed and
stored in CSV files.

### ResultAnalysis
The ResultAnalysis module is responsible for running logistic regression on the
Validator results. It also includes functionality to calculate statistics of the
Validator results. The statistics and logistic regression results are stored in
CSV files.

## Included data
THe data used and produced during the research is included in this repository.
- `data/projects` contains the Scala projects that have been analysed.
- `data/gitCache` contains a cached set of ids of the issues labelled as bug and
the pull-requests that refer to those issues.
- `data/metricResults` contains the measurement results CSVs produced by the
Validator.
- `data/analysisResults` contains the analysis resutls CSVs produced by the
ResultAnalysis.

## Build requirements
- [SBT](https://www.scala-sbt.org/) 1.3.13
- [Scala](https://www.scala-lang.org/) 2.13.3
- [Anaconda](https://www.anaconda.com/) 3.8 (for ResultAnalysis)

## Metric analysis process
The metric analysis process consists of the following steps:
1. Defining metrics
2. Selecting projects
3. Running the validation methodology
4. Running the result analysis

### Defining metrics
Metrics can be defined in the CodeAnalysis module. A basic metric definition
looks as follows:
```scala
import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric._

object ExampleMetric extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new ExampleMetric(compiler)
}

class ExampleMetric(override val compiler: Compiler)
  extends FileMetric
  with ObjectMetric
  with MethodMetric {

  import global.{TreeExtensions, SymbolExtensions}

  // File metrics
  override def run(tree: global.PackageDef): List[MetricResult] = List(
    MetricResult("MetricName", metricValue)
  )

  // Object metrics
  override def run(tree: global.ImplDef): List[MetricResult] = List(
    MetricResult("MetricName", metricValue)
  )

  // Method metrics
  override def run(tree: global.DefDef): List[MetricResult] = List(
    MetricResult("MetricName", metricValue)
  )
}
```
A metric consits of two components: a `MetricProducer` object and the metric
class itself. The `MetricProducer` is an object that will be used to instantiate
the metric during a compiler run.

The metric class can implement one or more of the following traits based on the
metric type: `FileMetric`, `ObjectMetric` and `MethodMetric`. Each trait has a
run method that receives a compiler tree matching to the metric type. By
implementing one of these traits the metric class gains access to the `compiler`
and `global` instances.

The `global` instance contains the tree types associated with the current
compiler run. The most important types are:
- `global.Tree` - The top-level tree type.
- `global.PackageDef` - A packaging tree, which contains all statements in the
file.
- `global.ImplDef` - Tree supertype used for classes/traits (`global.ClassDef`)
and objects (`global.ModuleDef`).
- `global.DefDef` - Tree type used for method definitions.

For more information about the different Scala tree types and their members,
please consult
the [Scala reference](https://docs.scala-lang.org/overviews/reflection/symbols-trees-types.html#trees), 
the [API documentation](https://www.scala-lang.org/api/2.13.3/scala-reflect/scala/reflect/api/Trees.html)
and take a look at this overview image (credits to Mirko Stocker):
![Scala trees](https://github.com/svenkonings/ScalaMetrics/raw/master/img/Scala_trees.png "Scala trees")

The `global` instance also contains helper and tree traversal methods which can
be accessed using `import global.{TreeExtensions, SymbolExtensions}`. For a list
of available methods see the [Source](https://github.com/svenkonings/ScalaMetrics/blob/master/CodeAnalysis/src/main/scala/codeAnalysis/analyser/Global.scala).
Below the implementation of the OutDegree metrics is shown:
```scala
package codeAnalysis.metrics.baseline

import codeAnalysis.analyser.Compiler
import codeAnalysis.analyser.metric.{MethodMetric, Metric, MetricProducer, MetricResult}

object OutDegree extends MetricProducer {
  override def apply(compiler: Compiler): Metric = new OutDegree(compiler)
}

class OutDegree(override val compiler: Compiler) extends MethodMetric {

  import global.TreeExtensions

  // Counts the number of method and function calls
  def outDegree(tree: global.DefDef): Int = tree.countTraverse {
    case _: global.Apply => true
  }

  // Counts the number of unique method and function calls
  def outDegreeDistinct(tree: global.DefDef): Int = tree.collectTraverse {
    case tree: global.Apply => tree.fun.symbol
  }.toSet.size

  override def run(tree: global.DefDef): List[MetricResult] = List(
    MetricResult("OutDegree", outDegree(tree)),
    MetricResult("OutDegreeDistinct", outDegreeDistinct(tree))
  )
}
```
The `countTraverse` method is one of the methods that can be accessed by
importing `TreeExtensions`. Given a [PartialFunction](https://www.scala-lang.org/api/2.13.3/scala/PartialFunction.html)
that returns a boolean, the `countTraverse` method counts all true matches. The
`collectTraverse` method is similar, it collects all matches.

The `compiler` instance can be used to check which filer are currently loaded
and possibly retrieve the compiler trees of those files if needed. For an
example, see the [CouplingBetweenObjects](https://github.com/svenkonings/ScalaMetrics/blob/master/CodeAnalysis/src/main/scala/codeAnalysis/metrics/baseline/CouplingBetweenObjects.scala)
or [NumberOfChildren](https://github.com/svenkonings/ScalaMetrics/blob/master/CodeAnalysis/src/main/scala/codeAnalysis/metrics/baseline/NumberOfChildren.scala)
implementations.

All metric definitions used in the research can be found in the
[CodeAnalysis metrics package](https://github.com/svenkonings/ScalaMetrics/tree/master/CodeAnalysis/src/main/scala/codeAnalysis/metrics).

### Measuring metrics
Metrics are measured using the Validator. The measurements are executed by
defining test cases. Test cases for the included projects are defined in the
[Validator UnitSpec](https://github.com/svenkonings/ScalaMetrics/blob/master/Validator/src/test/scala/validator/UnitSpec.scala).
To add new metrics create a tests that extends the UnitSpec and passes the
MetricProducers as follows:
```scala
class ExampleValidatorTest extends UnitSpec("output-folder", List(
  ExampleMetric // Add metric producers in this list
))
```
Run the test to gather results in the provided output folder.

To add projects to analyse, add a test case to the [Validator UnitSpec](https://github.com/svenkonings/ScalaMetrics/blob/master/Validator/src/test/scala/validator/UnitSpec.scala)
as follows and fill in the angle brackets:
```scala
test("<Test name>") {
  val validator = new Validator(
    "<repository owner>",
    "<repository name>",
    "<branch>",
    new File("data/projects/<git_folder_name>"),
    new File(s"data/metricResults/$folder/<output_folder_name>"),
    List("<issue_labels_used_for_bugs>"),
    metrics
  )
  validator.run()
}
```


All measurements used in the research can be found in the
[Validator test folder](https://github.com/svenkonings/ScalaMetrics/tree/master/Validator/src/test/scala/validator).

### Analysing metrics
In the ResultAnalysis folder run `python analysis` with the following arguments:
- `--folder` the name of the output folder to analyse (required)
- `--exclude-columns` space-separated list of columns to exclude from
multivariate regression analysis (optional)
- `--split-paradigm-score` split the analysed methods/objects by paradigm score
(optional)
- `--multivariate-baseline` run multivariate regression with the baseline
metrics included (optional)

The analysis commands used in the research are as follows:
- Paradigm score analysis
  - `python analysis --folder paradigmScoreBool`
  - `python analysis --folder paradigmScoreCount`
  - `python analysis --folder paradigmScoreFraction`
  - `python analysis --folder paradigmScoreLandkroon`
- Baseline model analysis
  - `python analysis --folder baseline --exclude-columns HasPointsFraction ParadigmScoreFraction`
  - `python analysis --folder baseline --split-paradigm-score --exclude-columns HasPointsFraction ParadigmScoreFraction`
- Candidate metric analysis
  - `python analysis --folder multiparadigm-zuilhof --multivariate-baseline --exclude-columns HasPointsFraction ParadigmScoreFraction`
  - `python analysis --folder multiparadigm-constructs --multivariate-baseline --exclude-columns HasPointsFraction ParadigmScoreFraction`

The HasPointFraction and ParadigmScoreFraction columns are included in the
baseline results to be able to split the results per paradigm. However, they
should be excluded from the baseline analysis themselves and are therefore added
to the `--exclude-columns` command for each analysis using the baseline results. 

### Plotting analysis results
Several plots have been made to visualise paradigm score and baseline results.
The plots can be found in the
[ResultAnalysis plots folder](https://github.com/svenkonings/ScalaMetrics/tree/master/ResultAnalysis/plots).
For the plots in the research the following commands have been used:
- `python plots/paradigm_score_plots.py --scatter-color --hist --write`
- `python plots/baseline_paradigm_plots.py --write`

## Credits
The initial design of this framework has been inspired by the
[SSCA](https://github.com/ERLKDev/SSCA) project.
