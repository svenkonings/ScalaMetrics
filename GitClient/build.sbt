name := "GitClient"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= List(
  "org.eclipse.jgit" % "org.eclipse.jgit" % "5.9.0.202009080501-r",
  "org.slf4j" % "slf4j-nop" % "1.7.30",
  "com.lihaoyi" %% "requests" % "0.6.5",
  "com.lihaoyi" %% "upickle" % "1.2.0",
  "org.scalatest" % "scalatest_2.13" % "3.2.0" % "test",
)
