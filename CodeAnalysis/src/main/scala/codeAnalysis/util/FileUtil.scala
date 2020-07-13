package codeAnalysis.util

import java.io.File

object FileUtil {
  def isTestPath(dir: String): Boolean = dir.contains(File.separator + "test" + File.separator)

  def isTestPath(dir: File): Boolean = isTestPath(dir.getPath)

  def isSourceFile(filename: String): Boolean = filename.endsWith(".scala") || filename.endsWith(".java")

  def isDirectory(parentDir: File, name: String): Boolean = new File(parentDir, name).isDirectory
}
