package ru.shvets.subscriber.bot.app.ktor

import io.ktor.server.application.*
import ru.shvets.subscriber.bot.app.ktor.config.AppSettings
import ru.shvets.subscriber.bot.app.ktor.config.DatabaseFactory
import ru.shvets.subscriber.bot.app.ktor.plugins.*

fun main(args: Array<String>): Unit =
    io.ktor.server.cio.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module(appSettings: AppSettings = initAppSettings()) {
    configureMonitoring()
    configureSerialization()
    DatabaseFactory.init(appSettings)
    initServices(appSettings)
    configureRouting()
}