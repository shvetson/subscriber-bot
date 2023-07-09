package ru.shvets.subscriber.bot.app.ktor.config

data class AppSettings(
    val db: PostgresConfig = PostgresConfig.NONE,
    val bot: TelegramConfig = TelegramConfig.NONE,
    val timerMinutes: Long,
//    val repo: UserRepository,
    val repo: List<Any>? = mutableListOf(),
)