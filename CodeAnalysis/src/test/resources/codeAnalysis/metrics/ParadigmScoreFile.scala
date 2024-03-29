class ParadigmScoreFile {
  def recursive(list: List[Int]): Int = list match {
    case Nil => 0
    case (a: Int) :: tail => a + recursive(tail)
  }

  case class Tree(value: Int, children: List[Tree])

  def recursive2(tree: Tree): Int = tree.value + tree.children.map(recursive2).sum

  def nested(string: String): Unit = {
    def myPrint(string: String): Unit = println(string)

    myPrint(string)
  }

  def tripleNested(string: String): Unit = {
    def doubleNested(string: String): Unit = {
      def singleNested(string: String): Unit = println(string)

      singleNested(string)
    }

    doubleNested(string)
  }

  def higherOrderParam(f: PartialFunction[String, Int]): Int = f("Hello World")

  def higherOrderCall(list: List[String]): List[String] = list.filter(!_.isEmpty)

  def higherOrderReturn(list: List[String]): Int => List[String] = i => list.take(i)

  def usesFunction: Int = (() => 3) ()

  def currying(f: Int => Int => Int): Int = f(1)(2)

  def patternMatch(value: Any): Boolean = value match {
    case _: Int => true
    case _ => false
  }

  lazy val lazyVal: String = "I'm lazy"

  def lazyValue: String = lazyVal

  def multipleParameterLists(x: Int)(y: Int): Int = x + y

  def variableDefinition(): Int = {
    var count: Int = 0
    count += 1
    count
  }

  var classCount: Int = 0

  def outerVariable(): Int = {
    classCount += 1
    classCount
  }

  def variableUsage: Int = classCount

  def sideEffects(list: List[Int]): Unit = list.foreach(println)

  def forWhileDo(list: List[Int]): Unit = {
    for (e <- list) println(e)
    var i = 0
    while (i < list.size) {
      println(list(i))
      i += 1
    }
    do {
      println(list(i))
      i -= 1
    } while (i >= 0)
  }

  def rangeTest: Unit = for (i <- 1 to 10 if i < 5) println(i)
}
