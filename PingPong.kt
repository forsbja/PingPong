import java.io.EOFException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.system.exitProcess

class Ball: Serializable {}
class ObjSocket(private val skt: Socket) {
    private val oout = ObjectOutputStream(skt.getOutputStream())
    private val oin = ObjectInputStream(skt.getInputStream())
    val remoteAddr = skt.remoteSocketAddress
    fun read(): Any = oin.readObject()
    fun write(obj: Any) {
        oout.writeObject(obj)
        oout.flush()
    }
    fun close() {
        oout.close()
        oin.close()
        skt.close()
    }
}

fun serveClient(skt: ObjSocket, gameNum: Int) {
    try {
        println("starting game $gameNum with ${skt.remoteAddr}")
        var server = false
        while (true) {
            Thread.sleep(1000)
            val flip = (0..1).random()
            val clientVal = skt.read() as Int
            if (clientVal == flip) { //Tie
                println("GAME $gameNum: coin toss tie; try again")
                skt.write("tie")
            } else if (clientVal > flip) { //Client won
                println("GAME $gameNum: lost the coin toss; returning serve")
                skt.write("win")
                break
            } else { //Server win
                println("GAME $gameNum: won the coin toss")
                skt.write("lose")
                server = true
                break
            }
        }

        if(server) {
            val temp = skt.read()
            println("GAME $gameNum: serve!")
            skt.write(temp)
        }

        while (true) {
            val ball = try {
                skt.read()
            } catch (e: EOFException) {
                // this exception signifies that the client has severed
                // the connection
                break
            }
            when(server) {
                true -> println("GAME $gameNum: received pong")
                false -> println("GAME $gameNum: received ping")
            }
            skt.write(ball)
            Thread.sleep(1000)
        }
    } catch (e: InterruptedException) {
        println(e)
    } catch (e: IOException) {
        println(e)
    }
}

fun client(skt: ObjSocket) {
    try {
        //Coin flip
        val ball = Ball()
        var server = false
        while(true) {
            Thread.sleep(1000)
            skt.write((0..1).random())
            val serverVal = skt.read() as String
            if( serverVal == "tie") {
                println("CLIENT: coin toss tie; try again")
                continue
            } else if (serverVal == "win") {
                println("CLIENT: won the coin toss")
                println("CLIENT: serve!")
                server = true
                break
            } else {
                println("CLIENT: lost the coin toss; returning serve")
                break
            }
        }
        skt.write(ball)

        //In game
        while(true){
            skt.read()
            when(server) {
                true -> println("CLIENT: received pong")
                false -> println("CLIENT: received ping")
            }
            skt.write(ball)
            Thread.sleep(1000)
        }
    } catch (e: InterruptedException) {
        println(e)
    } catch (e: IOException) {
        println(e)
            skt.close()
    }
}

fun server(port: Int) {
    try {
        val skt = ServerSocket(port)
        var gameNum = 0
        //Checking for and accepting new clients
        while (true) {
            val clientSkt = ObjSocket(skt.accept())
            val t = Thread { serveClient(clientSkt, gameNum) }
            // We want client threads to be lower priority than the main
            // server thread. To avoid possible starvation of the server
            // thread in case of high load, so we aren't rejecting new
            // connections.
            t.priority = Thread.currentThread().priority - 1
            t.start()
            gameNum++
        }
    } catch (e: Exception) {
        println(e)
    }
}

fun main(args: Array<String>) {
    val argSizes = mapOf("server" to 2, "client" to 3, "servent" to 4)
    if (args.isEmpty() || argSizes[args[0]] != args.size) {
        println("""Usage:
            PingPong server <port> 
            PingPong client <host> <port> 
            PingPong servent <host> <client_port> <svr_port> 
        """.trimIndent())
        exitProcess(1)
    }
    if (args[0] == "server") {
        server(args[1].toInt())
        return
    }

    val skt = try {
        ObjSocket(Socket(args[1], args[2].toInt()))
    } catch (e: InterruptedException) {
        println(e)
        return
    } catch (e: IOException) {
        println(e)
        return
    }

    if (args[0] == "client") {
        client(skt)
        skt.close()
        return
    }

    // servent
    val delay = 1000.toLong()
    val task = object : TimerTask() {
        override fun run() = client(skt)
    }
    Timer("ClientTimer").schedule(task, 0, delay)
    server(args[3].toInt())
}