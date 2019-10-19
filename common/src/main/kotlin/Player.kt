import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes


data class Player(val x: Short, val y: Short){
        fun toByteArray():ByteArray{
            val builder = BytePacketBuilder()
            builder.writeShort(x)
            builder.writeShort(y)
            return builder.build().readBytes()
        }

    }

fun toPlayer(byteArray: ByteArray): Player {
    val reader = ByteReadPacket(byteArray)
    return Player(reader.readShort(),reader.readShort())
}

fun toPlayers(byteArray: ByteArray): List<Player> {
    val players = ArrayList<Player>()
    val reader = ByteReadPacket(byteArray)
    while (reader.canRead()){
        players.add(Player(reader.readShort(),reader.readShort()))
    }
    return players
}