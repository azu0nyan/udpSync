package game.impl

import game.impl.GameImpl.{GameData, InputData}
import game.{Game, GameDrawer}

import java.awt.event.KeyEvent
import java.awt.{Color, Graphics2D}

object GameImpl {

  case class GameData(
                       p1x: Int, p1y: Int,
                       p2x: Int, p2y: Int,
                       p1dx: Int, p1dy: Int,
                       p2dx: Int, p2dy: Int,
                     ) extends Serializable

  case class InputData(dx: Int, dy: Int) extends Serializable

  val draw: (Graphics2D, GameData) => Unit = (g, d) => {
    g.setColor(Color.RED)
    g.fillOval(d.p1x - 5, d.p1y - 5, 10, 10)

    g.setColor(Color.GREEN)
    g.fillOval(d.p2x - 5, d.p2y - 5, 10, 10)
  }

  val processKey: (InputData, KeyEvent, Boolean) => InputData = (i, e, pressed) => {
    if (pressed) e.getKeyCode match {
      case KeyEvent.VK_A => i.copy(dx = -1)
      case KeyEvent.VK_D => i.copy(dx = 1)
      case KeyEvent.VK_W => i.copy(dy = -1)
      case KeyEvent.VK_S => i.copy(dy = 1)
      case _ => i
    } else e.getKeyCode match {
      case KeyEvent.VK_A => i.copy(dx = 0)
      case KeyEvent.VK_D => i.copy(dx = 0)
      case KeyEvent.VK_W => i.copy(dy = 0)
      case KeyEvent.VK_S => i.copy(dy = 0)
      case _ => i
    }
  }

  class GameImpl extends Game[GameData, InputData] {


    var myData: GameData = GameData(0, 0, 0, 0, 0, 0, 0, 0)

    override def init(): Unit = {
      myData = GameData(100, 100, 400, 400, 0, 0, 0, 0)
    }

    override def onNewPlayerConnected(id: Int): Unit = {

    }

    override def update(dt: Double): Unit = {
      myData = GameData(
        myData.p1x + myData.p1dx,
        myData.p1y + myData.p1dy,
        myData.p2x + myData.p2dx,
        myData.p2y + myData.p2dy,
        myData.p1dx,
        myData.p1dy,
        myData.p2dx,
        myData.p2dy,
      )
    }
    override def getData(): GameData = myData
    override def setData(t: GameData): Unit = {
      myData = t
    }
    override def setInput(id: Int, i: InputData): Unit = {
      if (id == 0) {
        myData = myData.copy(p1dx = i.dx, p1dy = i.dy)
      }

      if (id == 1) {
        myData = myData.copy(p2dx = i.dx, p2dy = i.dy)
      }
    }
  }
}
