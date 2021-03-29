package udp

import java.net.{DatagramPacket, DatagramSocket, InetAddress}

class Client(val buffSize: Int = 256,
             val address:InetAddress = InetAddress.getLocalHost,
             var port:Int = 4000,
            ) {
  val socket = new DatagramSocket()

  def send(msg:Array[Byte]):Unit = {
    val packet = new DatagramPacket(msg, msg.length, address, port)
    socket.send(packet)
  }
}
