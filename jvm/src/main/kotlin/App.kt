
import io.javalin.Javalin
import org.eclipse.jetty.websocket.api.Session
import java.nio.ByteBuffer

fun main(args: Array<String>) {
    val app = Javalin.create().apply {
        exception(Exception::class.java) { e, _ -> e.printStackTrace() }
        error(404) { ctx -> ctx.json("not found") }
    }.start(8080)

    app.config.addStaticFiles("/web")

    println(common())

    val sessionToPlayers = HashMap<Session, Player>()

    app.ws("/") { ws ->
        ws.onConnect { session ->
            run {
                println("Connected. " + session.host())

            }
        }
        ws.onBinaryMessage{ context ->
            val message = context.data()
            val session = context.session

            val byteArray = message.toByteArray()

            val player = toPlayer(byteArray)
            sessionToPlayers[session] = player

            val messagesFromOthers = sessionToPlayers.entries
                    .filter { e -> e.key != session }
                    .map { e -> e.value.toByteArray() }

            if(messagesFromOthers.isNotEmpty()){
                val state = messagesFromOthers
                        .reduce() { a, b -> a.plus(b) }
                session.remote.sendBytes(ByteBuffer.wrap(state))
                println("sent ${messagesFromOthers.size}B´s to ${session.remoteAddress}")
            }
        }
        ws.onClose { context ->
            val session = context.session

            run {
                sessionToPlayers.remove(session)
                println("Closed: " + session.remoteAddress)
            }
        }
        ws.onError { context ->
            println("Errored: " + context.session.remoteAddress) }
    }
}
