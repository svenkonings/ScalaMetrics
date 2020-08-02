package codeAnalysis.util

import java.io.File

object FileUtil {
  def isTestPath(dir: String): Boolean = dir.contains("test/")

  def isTestPath(dir: File): Boolean = isTestPath(dir.getPath.replace("\\", "/"))

  def isSourceFile(filename: String): Boolean = isScalaFile(filename) || isJavaFile(filename)

  def isScalaFile(filename: String): Boolean = filename.endsWith(".scala")

  def isJavaFile(filename: String): Boolean = filename.endsWith(".java")

  def isDirectory(parentDir: File, name: String): Boolean = new File(parentDir, name).isDirectory
}
