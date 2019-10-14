
import io.javalin.Javalin
import org.eclipse.jetty.websocket.api.Session

fun main(args: Array<String>) {
    val app = Javalin.create().apply {
        exception(Exception::class.java) { e, ctx -> e.printStackTrace() }
        error(404) { ctx -> ctx.json("not found") }
    }.start(8080)

    app.config.addStaticFiles("/web")

    println(common())

    val map = HashMap<Session, String>()

    app.ws("/") { ws ->
        ws.onConnect { session ->
            run {
                println("Connected. " + session.host())

            }
        }
        ws.onMessage { context ->
            val message = context.message()
            val session = context.session

            println("Received: " + message)

            map[session] = message

            val lastMessages = map.entries
                    .filter { e -> e.key != session }
                    .map { e -> e.value }

            if(lastMessages.isNotEmpty()){
                val messagesFromOthers = lastMessages.joinToString { s -> "$s+" }
                session.remote.sendString(messagesFromOthers)
                println("sent $messagesFromOthers to ${session.remoteAddress}")
            }
        }
        ws.onClose { context ->
            val session = context.session

            run {
                map.remove(session)
                println("Closed: " + session.remoteAddress)
            }
        }
        ws.onError { context ->
            println("Errored: " + context.session.remoteAddress) }
    }
}
