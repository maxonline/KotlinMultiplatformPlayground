import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes


data class PlayerPosition(val x: Short, val y: Short){
    constructor(byteArray: ByteArray) : this(ByteReadPacket(byteArray))
    private constructor(reader: ByteReadPacket) : this(
            x = reader.readShort(),
            y = reader.readShort()
    )

    fun toByteArray(): ByteArray {
        val builder = BytePacketBuilder()
        builder.writeShort(x)
        builder.writeShort(y)
        return builder.build().readBytes()
    }
}