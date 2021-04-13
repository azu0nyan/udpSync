package game

import java.lang.String
import java.net.{DatagramPacket, DatagramSocket, InetAddress}
import java.util.concurrent.{ConcurrentLinkedDeque, SynchronousQueue}

class GameServer[DATA <: Serializable, INPUT <: Serializable](
                                                               game: Game[DATA, INPUT],
                                                               val buffSize: Int = 1400,
                                                               val port: Short = 4000,
                                                             ) {
  var serverRunning = true
  val socket = new DatagramSocket(port)
  var players: Map[(InetAddress, Int), Int] = Map()

  var inputs = new ConcurrentLinkedDeque[(Int, INPUT)]()

  def waitPlayerConnection(): Unit = {
    println(s"Waiting player connection...")
    var connected = false
    while (!connected && serverRunning) {
      val buff = Array.ofDim[Byte](buffSize)
      val packet = new DatagramPacket(buff, buff.length)
      socket.receive(packet)

      val addr = packet.getAddress
      val port = packet.getPort
      val received = packet.getData.take(packet.getLength)
      val msg = new String(received)
      if (msg == Protocol.CLI_CONNECT_REQUEST) {
        if (!players.contains((addr, port))) {
          val id = players.size
          println(s"Connected player: $id $addr:$port")
          players += (addr, port) -> id
          connected = true
          game.onNewPlayerConnected(id)
        }
        sendConnectedResponse(addr, port)
      }
    }
  }

  def sendConnectedResponse(addr: InetAddress, port: Int): Unit = {
    val response = Protocol.SRV_CONNECT_ACCEPT.getBytes
    val packet = new DatagramPacket(response, response.length, addr, port)
    socket.send(packet)
  }

  def listenToPlayerInput(): Unit = {
    println(s"Listening to player input..")
    while (serverRunning) {
      val buff = Array.ofDim[Byte](buffSize)
      val packet = new DatagramPacket(buff, buff.length)
      socket.receive(packet)

      val addr = packet.getAddress
      val port = packet.getPort
      val received = packet.getData.take(packet.getLength)
      try {
        val id = players((addr, port))
        val input: INPUT = Conversions.fromByteArray(received)
        inputs.add((id, input))
      } catch {
        case t: Throwable =>
          val maybeConnected = new String(received)
          if (maybeConnected == Protocol.CLI_CONNECT_REQUEST) {
            sendConnectedResponse(addr, port)
          } else t.printStackTrace()
      }
    }
  }

  def startServer(): Unit = {
    //Ждем подключения двух игроков
    waitPlayerConnection()
    waitPlayerConnection()

    println(s"All players connected. Initializing game..")
    //Запускаем поток слушающий сообщения от игроков
    new Thread(() => listenToPlayerInput(), "Player input").start()

    game.init()
    println(s"Starting game loop")
    var lastFrame = System.currentTimeMillis()
    val frameTime = 1000 / 60
    while (serverRunning) {
      val currentFrame = System.currentTimeMillis()
      val dt = currentFrame - lastFrame
      lastFrame = currentFrame
      // Обрабатываетм полученный ввод игроков
      while (!inputs.isEmpty) {
        val (i, id) = inputs.pop()
        game.setInput(i, id)
      }
      //обновляем состояние мира
      game.update(1.0 / 60)
      //отправляем данные игрокам
      sendDataToPlayers()

      Thread.sleep(math.max(0, frameTime - dt))
    }
  }

  def sendDataToPlayers(): Unit = {
    val data = game.getData()
    val serialized: Array[Byte] = Conversions.toByteArray(data)
    for (((addr, port), id) <- players) {
      val packet = new DatagramPacket(serialized, serialized.length, addr, port)
      socket.send(packet)
    }
  }


}
