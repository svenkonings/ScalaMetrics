package gitclient.util

import java.io._

object Cache {
  val prefix: String = "target/"
  val extension: String = ".obj"

  def fullFilename(filename: String): String = prefix + filename + extension

  def isCached(filename: String): Boolean = new File(fullFilename(filename)).exists()

  def readObject(filename: String): AnyRef = {
    var input: ObjectInputStream = null
    try {
      input = new ObjectInputStream(new FileInputStream(fullFilename(filename)))
      input.readObject()
    } finally {
      if (input != null) input.close()
    }
  }

  def writeObject(filename: String, serializable: Serializable): Unit = {
    var output: ObjectOutputStream = null
    try {
      output = new ObjectOutputStream(new FileOutputStream(fullFilename(filename)))
      output.writeObject(serializable)
    } finally {
      if (output != null) output.close()
    }
  }
}
