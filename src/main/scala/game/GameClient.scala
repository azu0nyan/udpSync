package game

import java.awt.event.{KeyEvent, KeyListener}
import javax.swing._
import java.awt._
import java.net.{DatagramPacket, DatagramSocket, InetAddress, SocketTimeoutException}

class GameClient[DATA <: Serializable, INPUT <: Serializable](
                                                               var data:DATA,
                                                               var input:INPUT,
                                                               drawer: (Graphics2D, DATA) => Unit,
                                                               processKey: (INPUT, KeyEvent, Boolean) => INPUT,
                                                               val w: Int = 800,
                                                               val h: Int = 600,
                                                               host: InetAddress,
                                                               port: Int = 4000,
                                                               val buffSize: Int = 1400,
                                                             ) extends JFrame {

  var gameRunning = true
  val server = new DatagramSocket()


  //Инициализация окна
  setSize(w, h) //размер экрана
  setUndecorated(false); //показать заголовок окна
  setTitle("Udp client");
  setVisible(true);
  setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
  createBufferStrategy(2)

  addKeyListener(new KeyListener {
    override def keyTyped(keyEvent: KeyEvent): Unit = {}
    override def keyPressed(keyEvent: KeyEvent): Unit = {
      input = processKey(input,keyEvent, true )
    }
    override def keyReleased(keyEvent: KeyEvent): Unit = {
      input = processKey(input,keyEvent, false )
    }
  })
  connectToServer()

  new Thread(() => renderingLoop(), "Rendering loop").start()
  new Thread(() => readingFromServerLoop(), "Reading from server loop").start()

  def sendInput():Unit = {
    val toSend = Conversions.toByteArray(input)
    val packet = new DatagramPacket(toSend, toSend.size, host, port)
    server.send(packet)
  }

  def readingFromServerLoop():Unit = {
    while (gameRunning){
      val packet = new DatagramPacket(new Array[Byte](buffSize), buffSize, host, port)
      server.receive(packet)
      data = Conversions.fromByteArray(packet.getData.take(packet.getLength))
      println(s"Received data : $data")
    }
  }

  def renderingLoop() :Unit = {
    import java.awt.Graphics2D
    import java.awt.image.BufferStrategy
    var lastFrame = System.currentTimeMillis()
    val frameTime = 1000 / 60
    while(gameRunning) {
      val currentFrame = System.currentTimeMillis()
      val dt = currentFrame - lastFrame
      lastFrame = currentFrame

      val bs: BufferStrategy = getBufferStrategy
      val g: Graphics2D = bs.getDrawGraphics.asInstanceOf[Graphics2D]
      g.clearRect(0, 0, getWidth, getHeight)

      drawer(g, data)

      bs.show()
      sendInput()

      Thread.sleep(math.max(0, frameTime - dt))
    }
  }


  def connectToServer(): Unit = {
    var connected = false
    var attempts = 0
    while (!connected) {
      attempts += 1
      println(s"Connecting to $host:$port attempt: $attempts ...")
      val data = Protocol.CLI_CONNECT_REQUEST.getBytes
      val packet = new DatagramPacket(data, data.length, host, port)
      server.send(packet)
      try {
        server.setSoTimeout(1000)
        val respPacket = new DatagramPacket(new Array[Byte](buffSize), buffSize)
        server.receive(respPacket)
        val resp = new String(respPacket.getData.take(respPacket.getLength))
        if (resp == Protocol.SRV_CONNECT_ACCEPT) {
          connected = true
          println(s"Connection to server established.")
        } else {
          println(s"No ACCEPT response from server.")
        }
      } catch {
        case st: SocketTimeoutException =>
          println(s"Connection timeout..")
      }
    }
    server.setSoTimeout(0)

  }

}
