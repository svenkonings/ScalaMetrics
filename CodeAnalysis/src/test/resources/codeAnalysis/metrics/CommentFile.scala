class CommentFile {
  /**
   * Some lovely documentation
   * @param list
   * @return
   */
  def codeWithComment(list: List[Int]): Int = list match {
    case Nil => 0 /* Should always be reached */
      // Do this recusively
    case (a: Int) :: tail => a + codeWithComment(tail)
  }
}
