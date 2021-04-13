import game.GameServer
import game.impl.GameImpl.GameImpl

object RunGameServer extends App{
  val srv = new GameServer(
    new GameImpl
  )
  srv.startServer()

}
