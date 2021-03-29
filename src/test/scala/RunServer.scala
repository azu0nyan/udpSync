import udp.Server

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object RunServer extends App{
  val s = new Server()
  s.run()

}
