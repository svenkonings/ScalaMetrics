package gitclient.util

import java.io._

import scala.util.Using.resource

object Cache {
  val prefix: String = "data/gitCache/"
  val extension: String = ".obj"

  def fullFilename(filename: String): String = prefix + filename + extension

  def isCached(filename: String): Boolean = new File(fullFilename(filename)).exists()

  def readObject(filename: String): AnyRef =
    resource(new ObjectInputStream(new FileInputStream(fullFilename(filename))))(_.readObject())

  def writeObject(filename: String, serializable: Serializable): Unit = {
    val file = new File(fullFilename(filename))
    file.getParentFile.mkdirs()
    resource(new ObjectOutputStream(new FileOutputStream(file)))(_.writeObject(serializable))
  }
}
