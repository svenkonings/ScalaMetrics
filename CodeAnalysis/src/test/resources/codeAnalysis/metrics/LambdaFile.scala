class LambdaFile {
  var outer = 0
  def outerVarLambda(list: List[Int]): List[List[Int]] =
    list.map(i => i.to(outer).toList)

  def outerVarLambdaSide(list: List[Int]): Int = {
    list.foreach(i => outer += i)
    outer
  }

  def localVarLambda(list: List[Int]): List[List[Int]] = {
    var local = 0
    list.map(i => i.to(local).toList)
  }

  def localVarLambdaSide(list: List[Int]): Int = {
    var local = 0
    list.foreach(i => local += i)
    local
  }

  def innerVarLambda(list: List[Int]): List[List[Int]] = list.map(i => {
    var inner = 0
    i.to(inner).toList
  })

  def innerVarLambdaSide(list: List[Int]): Unit = list.foreach(i => {
    var inner = 0
    inner += i
  })
}