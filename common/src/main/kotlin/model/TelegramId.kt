package ru.shvets.subscriber.bot.common.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * @author  Oleg Shvets
 * @version 1.0
 * @date  13.03.2023 13:12
 */

@Serializable
@JvmInline
value class TelegramId(private val id: String){
    fun asString() = id

    companion object {
        val NONE = TelegramId("")
    }
}