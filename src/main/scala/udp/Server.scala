package udp

import java.net.{DatagramPacket, DatagramSocket}

class Server(val buffSize: Int = 256,
             val port: Short = 4000,
            ) extends Runnable {
  val socket = new DatagramSocket(port)


  var running: Boolean = true

  def run(): Unit = {
    println(s"Server running ...")
    while (running) {
      val buff = Array.ofDim[Byte](buffSize)
      val packet = new DatagramPacket(buff, buff.length)
      socket.receive(packet)

      val addr = packet.getAddress
      val port = packet.getPort
      val received = packet.getData
      println(new String(s"${addr.getHostAddress}:$port ${new String(received)}" ))

      socket.send(new DatagramPacket("OK".getBytes, "OK".getBytes.length, addr, port))
    }
  }

}
