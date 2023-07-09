package ru.shvets.subscriber.bot.app.ktor.config

/**
 * @author  Oleg Shvets
 * @version 1.0
 * @date  20.06.2023 11:57
 */

data class TelegramConfig(
    val botToken: String,
    val botName: String,
    val botOwner: Long,
    val privateChannel: String,
) {
    companion object {
        val NONE = TelegramConfig(
            botName = "",
            botToken = "",
            botOwner = 0,
            privateChannel = ""
        )
    }
}