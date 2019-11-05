import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes


@ExperimentalUnsignedTypes
data class Player(
        val x: Short,
        val y: Short,
        val distance: Short,
        val red: UByte,
        val green: UByte,
        val blue: UByte,
        val score: Byte) {

    constructor(byteArray: ByteArray) : this(ByteReadPacket(byteArray))
    constructor(reader: ByteReadPacket) : this(
            x = reader.readShort(),
            y = reader.readShort(),
            distance = reader.readShort(),
            red = reader.readByte().toUByte(),
            green = reader.readByte().toUByte(),
            blue = reader.readByte().toUByte(),
            score = reader.readByte()
    )
    fun buildBytes(builder: BytePacketBuilder) {
        builder.writeShort(x)
        builder.writeShort(y)
        builder.writeShort(distance)
        builder.writeUByte(red)
        builder.writeUByte(green)
        builder.writeUByte(blue)
        builder.writeByte(score)
    }

    private fun BytePacketBuilder.writeUByte(uByte: UByte) = writeByte(uByte.toByte())

    fun toByteArray(): ByteArray {
        val builder = BytePacketBuilder()
        buildBytes(builder)
        return builder.build().readBytes()
    }
}

@ExperimentalUnsignedTypes
fun toByteArray(playerPositions: List<Player>): ByteArray {
    val builder = BytePacketBuilder()
    playerPositions.forEach { it.buildBytes(builder) }
    return builder.build().readBytes()
}

@ExperimentalUnsignedTypes
fun toPlayers(byteArray: ByteArray): List<Player> {
    val players = ArrayList<Player>()
    val reader = ByteReadPacket(byteArray)
    while (reader.canRead()) {
        players.add(Player(reader))
    }
    return players
}