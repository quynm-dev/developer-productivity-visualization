package com.dpv.config

import io.ktor.server.application.*
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

interface ApplicationConfigurer {
    fun configure()
}

fun Application.configureDI() {
    install(Koin) {
        slf4jLogger()
        modules(environmentModule(), defaultModule)
    }

    val koin = getKoin()
    koin.getAll<ApplicationConfigurer>().forEach { configurer ->
        configurer.configure()
    }
}

fun Application.environmentModule() = module {
    single { environment } bind(ApplicationEnvironment::class)
}