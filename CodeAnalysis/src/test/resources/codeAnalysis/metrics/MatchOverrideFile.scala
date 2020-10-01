class MatchOverrideFile {
  val hello: String = "Hello world!"

  def matchOverrideFile(value: Any): String = value match {
    case Tuple1(hello) => "Override1"
    case hello => "Override2"
  }
}