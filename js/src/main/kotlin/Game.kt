import games.perses.game.DrawMode
import games.perses.game.Game
import games.perses.game.Screen
import games.perses.input.EmptyInputProcessor
import games.perses.input.Input
import games.perses.sprite.Sprite
import games.perses.sprite.SpriteBatch
import games.perses.text.Texts
import games.perses.texture.Textures
import org.w3c.dom.*
import kotlin.browser.document
import kotlin.math.sin


class GameScreen : Screen() {
    var webSocket = WebSocket("ws://"  + document.location?.host)

    var sprites = SpriteBatch()

    var sprite = Sprite("smiley")
    var x = 0f
    var y = 0f

    var onScreenText = "hej"
    var xyText = "$x,$y";


    var timeSinceLastSend = 0f;

    var players: List<Pair<Float, Float>> = emptyList()

    override fun loadResources() {
        Textures.load("smiley", "img/smiley.png")
    }

    override fun closeResources() {
        Textures.dispose()
    }



    override fun update(time: Float, delta: Float) {
        Game.clearRed = sin((time / 3).toDouble()).toFloat()
        Game.clearGreen = sin((time / 5).toDouble()).toFloat()

        xyText = "$x,$y";
        onScreenText = Game.fps.toString() + " " + xyText;

        timeSinceLastSend += delta

        val intervalBetweenSends = 0.03f
        if (timeSinceLastSend > intervalBetweenSends) {
            timeSinceLastSend = timeSinceLastSend % intervalBetweenSends
            val openState: Short = 1
            if (webSocket.readyState == openState) {
                webSocket.send(xyText);
            }
        }
    }

    override fun render() {
        sprites.draw(sprite, x, y, scale = 0.1f)

        players.forEach { player -> sprites.draw(sprite, player.first, player.second, scale = 0.06f )}

        sprites.render()

        Texts.drawText(0f, Game.view.height - 20, onScreenText, font = "bold 20pt Comic Sans", fillStyle = "black")
        Texts.drawText(0f, Game.view.height - 60, common(), font = "bold 20pt Comic Sans", fillStyle = "black")
    }

}

fun main(args: Array<String>) {
    // set border color
    document.body?.style?.backgroundColor = "#242"

    Game.view.setToWidth(1200f)
    Game.view.drawMode = DrawMode.LINEAR

    Game.view.minAspectRatio = 1200f / 1400f
    Game.view.maxAspectRatio = 1200f / 800f

    Game.setClearColor(0f, 0f, 0.5f, 0.5f)

    val gameScreen = GameScreen()

    Input.setInputProcessor(object : EmptyInputProcessor() {
        override fun mouseMove(x: Float, y: Float) {
            gameScreen.x = x;
            gameScreen.y = y;
        }

        override fun pointerClick(pointer: Int, x: Float, y: Float) {
            println("Mouse click: $pointer -> $x, $y")
        }
    })

    gameScreen.webSocket.onmessage = { it ->
        if (it is MessageEvent) {
            val data:String = it.data as String

            val players = data.split("+").filter { s -> s.isNotBlank() }

            gameScreen.players = players.map { s ->

                val zys = s.split(",")
                zys[0].toFloat() to zys[1].toFloat()
            }

        }
    }

    gameScreen.webSocket.onerror = {
        if (it is ErrorEvent) {
            val data = it.message
            println("error! $data")
        }
        println("errror! ${it.type}")
    }

    Game.start(gameScreen)
}
