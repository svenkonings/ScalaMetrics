name := "ScalaMetrics"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies += "org.scalatest" % "scalatest_2.13" % "3.2.0" % "test"

lazy val root = project.in(file(".")).aggregate(Validator, CodeAnalysis, GitClient)

lazy val Validator = project.dependsOn(CodeAnalysis, GitClient)
lazy val CodeAnalysis = project
lazy val GitClient = project