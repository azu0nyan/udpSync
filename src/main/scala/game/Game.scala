package game

trait Game[DATA <: Serializable, INPUT <: Serializable] {

  def init(): Unit
  def onNewPlayerConnected(id: Int): Unit

  def update(dt: Double): Unit

  def getData(): DATA
  def setData(t: DATA): Unit

  def setInput(id: Int, i: INPUT): Unit

}

