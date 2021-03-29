import udp.Client

object RunClient extends App {
  val client = new Client


  while (true){
    val in = scala.io.StdIn.readLine()
    client.send(in.getBytes)
  }
}
