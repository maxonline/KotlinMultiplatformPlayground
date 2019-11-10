# Kotlin Multiplayer Web Game Template

Template project for multiplayer web games in Kotlin.

Using technologies:
* [Javalin](https://github.com/tipsy/javalin)
* [Kudens](https://github.com/perses-games/kudens)
* [Kotlinx-io](https://github.com/Kotlin/kotlinx-io)

## Structure
* **Server** (see [/jvm](jvm/)) 

   Javalin websocket game server that also serves the static js code and assets from the client build.
* **Client** (see [/js](js/)) 

   Game client that uses the game lib Kudens. Generates js code from Kotlin.
* **Common** (see [/common](common/)) 

   Shared code containing a custom server-client message protocol using Kotlinx-io.

## Build
You can build and run the application from the root directory:
```
./gradlew run
```
Then go to ``http://localhost:8080/``
