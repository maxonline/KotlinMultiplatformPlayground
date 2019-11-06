import io.javalin.Javalin
import org.eclipse.jetty.websocket.api.Session
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.math.sqrt
import kotlin.random.nextUBytes

val log: Logger = LoggerFactory.getLogger("main")
@ExperimentalUnsignedTypes
fun main() {
    val app = Javalin.create().apply {
        exception(Exception::class.java) { e, _ -> log.error("Javalin error", e) }
        error(404) { ctx -> ctx.json("not found") }
    }.start(8080)

    app.config.addStaticFiles("/web")

    val sessionToPlayers = HashMap<Session, Player>()
    var currentGoalPixel = newGoalPixel()

    app.ws("/") { ws ->
        ws.onConnect { session ->
            run {
                log.info("New player connected: ${session.host()}")
            }
        }
        ws.onBinaryMessage { context ->
            val byteArray = context.data().toByteArray()
            val session = context.session
            val playerPosition = PlayerPosition(byteArray)

            val distanceToGoal = getDistance(playerPosition.x, playerPosition.y, currentGoalPixel)
            val player = sessionToPlayers[session] ?: createNewPlayer(playerPosition, distanceToGoal.toShort())
            sessionToPlayers[session] = player.copy(
                    x = playerPosition.x,
                    y = playerPosition.y,
                    distance = distanceToGoal.toShort()
            )
            if (distanceToGoal == 0) {
                sessionToPlayers[session] = player.copy(score = player.score.inc())

                currentGoalPixel = newGoalPixel()
                sessionToPlayers.putAll(sessionToPlayers.map { entry ->
                    val distanceToNewGoal = getDistance(entry.value.x, entry.value.y, currentGoalPixel)
                    entry.key to entry.value.copy(distance = distanceToNewGoal.toShort())
                })

            }

            session.remote.sendBytes(ByteBuffer.wrap(toByteArray(sessionToPlayers.values.toList())))
        }
        ws.onClose { context ->
            val session = context.session
            sessionToPlayers.remove(session)
            log.info("Disconnected: ${session.remoteAddress}")
        }
        ws.onError { context ->
            log.warn("Got Websocket error from: ${context.session.remoteAddress}", context.error())
        }
    }

}

fun newGoalPixel(): Pair<Short, Short> {
    val pixel = Pair(Random.nextInt(0, 1200).toShort(), Random.nextInt(0, 800).toShort())
    log.info("New goal: $pixel")
    return pixel
}

fun createNewPlayer(playerPosition: PlayerPosition, closeness: Short): Player {
    val random = Random
    return Player(
            x = playerPosition.x,
            y = playerPosition.y,
            distance = closeness,
            red = random.nextUBytes(1)[0],
            green = random.nextUBytes(1)[0],
            blue = random.nextUBytes(1)[0],
            score = 0
    )
}

fun getDistance(x1: Short, y1: Short, xy2: Pair<Short, Short>): Int {
    val x2 = xy2.first
    val y2 = xy2.second
    return sqrt((x1.toDouble() - x2).pow(2.0) + (y1.toDouble() - y2).pow(2.0)).roundToInt()
}
