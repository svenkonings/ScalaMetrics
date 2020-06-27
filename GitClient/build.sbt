name := "GitClient"

version := "0.1"

scalaVersion := "2.13.2"

libraryDependencies ++= List(
  "org.eclipse.jgit" % "org.eclipse.jgit" % "5.8.0.202006091008-r",
  "com.lihaoyi" %% "requests" % "0.5.1",
  "com.lihaoyi" %% "upickle" % "0.9.5",
  "org.scalatest" % "scalatest_2.13" % "3.2.0" % "test",
)
