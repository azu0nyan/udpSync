package game

object Conversions {
  def toByteArray[T <: Serializable](t:T):Array[Byte] = {
    import java.io.ByteArrayOutputStream
    import java.io.ObjectOutputStream
    val byteArrayOutputStream = new ByteArrayOutputStream
    val objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)
    objectOutputStream.writeObject(t)
    objectOutputStream.flush()
    byteArrayOutputStream.toByteArray
  }

  def fromByteArray[T <: Serializable](a:Array[Byte]):T = {
    import java.io.ByteArrayInputStream
    import java.io.ObjectInputStream
    val objectInputStream = new ObjectInputStream(new ByteArrayInputStream(a))
    val result = objectInputStream.readObject.asInstanceOf[T]
    objectInputStream.close()
    result
  }
}
