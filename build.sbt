name := "ScalaMetrics"

version := "0.1"

scalaVersion := "2.13.2"

lazy val root = project.in(file(".")).dependsOn(CodeAnalysis, GitClient)

lazy val CodeAnalysis = project
lazy val GitClient = project