package `in`.daram.nutcracker.app

import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureApp()
    }.start(wait = true)
}
