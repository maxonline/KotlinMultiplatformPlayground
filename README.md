# Kotlin Multiplayer Web Game Template

Template project for creating Kotlin web games with Websocket server and shared client-server code.

Using technologies:
* [Javalin](https://github.com/tipsy/javalin)
* [Kudens](https://github.com/perses-games/kudens)
* [Kotlinx-io](https://github.com/Kotlin/kotlinx-io)

## Structure
* **Server** (see [/jvm](jvm/)) 

   Javalin websocket server that also serves the static js code and assets from the client build.
* **Client** (see [/js](js/)) 

   Game base that uses Kudens and generates js code from Kotlin.
* **Common** (see [/common](common/)) 

   Shared code containing server-client message data classes and to/from byteArray functions using Kotlinx-io.

## Build
You can build and run the application from the root directory:
```
./gradlew run
```
Then go to ``http://localhost:8080/``