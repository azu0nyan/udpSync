import game.impl.GameImpl
import game.impl.GameImpl.{GameData, InputData}
import game.{Conversions, GameClient}

import java.net.InetAddress

object RunGameClient extends App{
  val c = new GameClient[GameData, InputData](
    GameData(0, 0, 0, 0,0,0,0,0),
    InputData(0, 0),
    GameImpl.draw,
    GameImpl.processKey,
    host = InetAddress.getLocalHost
  )
}
