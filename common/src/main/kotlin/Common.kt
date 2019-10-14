
// This code is common across all backends
class Common {
    fun common(): String = "common"
    fun platform(): String = Platform().platform() // delegates to platform-specific
}

fun common(): String = "common code is the shit"