package ru.shvets.subscriber.bot.app.ktor.plugins

import io.ktor.server.application.*
import ru.shvets.subscriber.bot.app.ktor.bot.BotService
import ru.shvets.subscriber.bot.app.ktor.config.AppSettings
import java.util.*

/**
 * @author  Oleg Shvets
 * @version 1.0
 * @date  21.06.2023 20:48
 */

fun Application.initServices(appSettings: AppSettings) {

    val botService = BotService(appSettings)
    botService.start()

//    val timer: Timer = Timer()
//    timer.schedule(object : TimerTask() {
//        override fun run() {
//            Runnable { runBlocking { botService.sendAds() } }.run()
//        }
//    }, 60 * 1000, appSettings.timerMinutes * 60 * 1000)
}