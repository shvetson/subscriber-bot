package ru.shvets.subscriber.bot.app.ktor.config

data class PostgresConfig(
    val url: String,
    val driver: String,
    val user: String,
    val password: String,
) {
    companion object {
        val NONE = PostgresConfig(
            url = "",
            driver = "",
            user = "",
            password = "",
        )
    }
}