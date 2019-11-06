import games.perses.game.DrawMode
import games.perses.game.Game
import games.perses.game.Game.html
import games.perses.game.Screen
import games.perses.input.EmptyInputProcessor
import games.perses.input.Input
import games.perses.text.Texts
import org.khronos.webgl.*
import org.w3c.dom.*
import org.w3c.files.Blob
import org.w3c.files.FileReader
import kotlin.browser.document
import kotlin.math.roundToInt
import kotlin.math.sqrt

@ExperimentalUnsignedTypes
fun main() {
    document.body?.style?.backgroundColor = "#182"

    Game.view.setToWidth(1200f)
    Game.view.drawMode = DrawMode.LINEAR

    Game.view.minAspectRatio = 1200f / 800f
    Game.view.maxAspectRatio = 1200f / 800f

    Game.setClearColor(0f, 0f, 0f, 1f)

    val gameScreen = GameScreen()

    Input.setInputProcessor(object : EmptyInputProcessor() {
        override fun mouseMove(x: Float, y: Float) {
            gameScreen.playerX = x.roundToInt().toShort()
            gameScreen.playerY = y.roundToInt().toShort()
        }
    })

    gameScreen.webSocket.onmessage = { messageEvent ->
        messageEvent.readBytes { bytes ->
            gameScreen.players = toPlayers(bytes)
        }
    }

    gameScreen.webSocket.onerror = {
        if (it is ErrorEvent) {
            val data = it.message
            println("ERROR: $data")
        } else {
            println("UNKNOWN ERROR: ${it.type}")
        }
    }

    Game.start(gameScreen)
}

@ExperimentalUnsignedTypes
class GameScreen : Screen() {
    val webSocket = WebSocket("ws://" + document.location?.host)

    var playerX: Short = 0
    var playerY: Short = 0
    var players: List<Player> = emptyList()

    private var secondsSinceLastSend = 0f
    private var onScreenText = ""

    override fun update(time: Float, delta: Float) {
        onScreenText = "fps: ${Game.fps} : ${playerX},${playerY}"

        secondsSinceLastSend += delta

        val secondsBetweenSends = 0.03f
        if (secondsSinceLastSend > secondsBetweenSends) {
            secondsSinceLastSend %= secondsBetweenSends
            val openstate: Short = 1
            if (webSocket.readyState == openstate) {
                val byteArray = PlayerPosition(playerX, playerY).toByteArray()
                webSocket.send(Int8Array(byteArray.toTypedArray()))
            }
        }
    }

    override fun render() {
        val context = html.canvas2d

        Texts.drawText(
                x = 10f,
                y = Game.view.height - 30,
                message = onScreenText,
                font = "bold 20pt Arial",
                fillStyle = "gray"
        )
        players.sortedByDescending { it.score }.forEachIndexed { i, player ->
            val y = Game.view.height - (40 * i + 80)
            context.drawBoxAt(player, 30, (y + 11).toShort(), 24.0)
            Texts.drawText(
                    x = 60f,
                    y = y,
                    message = player.score.toString(),
                    font = "bold 20pt Arial",
                    fillStyle = "gray"
            )
        }

        players.forEach { player ->
            val distanceToGoal = player.distance.toDouble()
            val width = sqrt(distanceToGoal*25)
            context.drawBoxAt(player, player.x, player.y, width)
        }

    }

    private fun CanvasRenderingContext2D.drawBoxAt(player: Player, x: Short, y: Short, width: Double) {
        fillStyle = "rgba(${player.red} ,${player.green} ,${player.blue}, 0.5 )"
        val leftX = x - width / 2
        val topY = y + width / 2

        val convertedY = Game.view.height - topY
        fillRect(leftX, convertedY, width, width)
    }

}

@ExperimentalUnsignedTypes
private fun MessageEvent.readBytes(onReadDone: (ByteArray) -> Unit) {
    val data: Blob = data as Blob

    val fileReader = FileReader()
    fileReader.addEventListener("loadend", {
        val arrayBuffer = fileReader.result as ArrayBuffer
        val uint8Array = Uint8Array(arrayBuffer)
        val byteArray = ByteArray(uint8Array.byteLength) { index -> uint8Array[index] }

        onReadDone(byteArray)
    })
    fileReader.readAsArrayBuffer(data)
}