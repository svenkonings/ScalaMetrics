package codeAnalysis.util

object Extensions {

  implicit class DoubleExtension(double: Double) {
    def \(other: Double): Double = if (other != 0.0) double / other else 0.0
  }

  implicit class BooleanExtension(boolean: Boolean) {
    def toInt: Int = if (boolean) 1 else 0
  }

  implicit class NumericTupleExtension[A: Numeric, B: Numeric](t: (A, B)) {

    import Numeric.Implicits._

    def +(p: (A, B)): (A, B) = (p._1 + t._1, p._2 + t._2)
  }

  implicit class ListExtension[A](iterable: List[A]) {
    def zipWith[B, C](other: List[B])(f: (A, B) => C): List[C] = iterable.zip(other).map(f.tupled)
  }

}
