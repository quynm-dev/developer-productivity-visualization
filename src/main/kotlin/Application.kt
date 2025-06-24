package com.dpv

import com.dpv.config.configureDB
import com.dpv.config.configureDI
import com.dpv.config.configureRouting
import com.dpv.config.configureSerialization
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureDB()
    configureDI()
    configureRouting()
    configureSerialization()
}
