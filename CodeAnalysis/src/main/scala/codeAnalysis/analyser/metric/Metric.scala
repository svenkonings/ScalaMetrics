package codeAnalysis.analyser.metric

import codeAnalysis.analyser.{Compiler, Global}

/**
 * Used to instantiate a metric during a compiler run.
 */
trait MetricProducer {
  /**
   * Create a new metric instance for the specified compiler.
   *
   * @param compiler the specified compiler
   * @return the created metric instance
   */
  def apply(compiler: Compiler): Metric
}

/**
 * A metric associated with a compiler run.
 */
trait Metric {
  /**
   * The compiler this metric is associated with.
   */
  val compiler: Compiler

  /**
   * The global instance, which provides a set of types and operations for this compiler run.
   *
   * @see `scala.reflect.api.Universe`
   */
  val global: Global = compiler.global
}

/**
 * A metric that can be applied to package trees (which contain all statement within a file).
 */
trait FileMetric extends Metric {
  /**
   * Casts the specified tree to the global instance of this metric and runs the metric.
   * The tree should belong to the same compiler run as this metric.
   *
   * @param tree the specified tree
   * @return the list of metric results
   */
  def apply(tree: Global#PackageDef): List[MetricResult] =
    run(tree.asInstanceOf[global.PackageDef])

  /**
   * Runs this metric on the specified tree
   *
   * @param tree the specified tree
   * @return the list of metric results
   */
  def run(tree: global.PackageDef): List[MetricResult]
}

/**
 * A metric that can be applied to impl trees (which is a supertype of both class and object trees).
 */
trait ObjectMetric extends Metric {
  /**
   * Casts the specified tree to the global instance of this metric and runs the metric.
   * The tree should belong to the same compiler run as this metric.
   *
   * @param tree the specified tree
   * @return the list of metric results
   */
  def apply(tree: Global#ImplDef): List[MetricResult] =
    run(tree.asInstanceOf[global.ImplDef])

  /**
   * Runs this metric on the specified tree
   *
   * @param tree the specified tree
   * @return the list of metric results
   */
  def run(tree: global.ImplDef): List[MetricResult]
}

/**
 * A metric that can be applied to method definition trees.
 */
trait MethodMetric extends Metric {
  /**
   * Casts the specified tree to the global instance of this metric and runs the metric.
   * The tree should belong to the same compiler run as this metric.
   *
   * @param tree the specified tree
   * @return the list of metric results
   */
  def apply(tree: Global#DefDef): List[MetricResult] =
    run(tree.asInstanceOf[global.DefDef])

  /**
   * Runs this metric on the specified tree
   *
   * @param tree the specified tree
   * @return the list of metric results
   */
  def run(tree: global.DefDef): List[MetricResult]
}
