package ru.shvets.subscriber.bot.common.model

import kotlinx.datetime.Instant
import ru.shvets.subscriber.bot.common.helper.NONE

/**
 * @author  Oleg Shvets
 * @version 1.0
 * @date  08.07.2023 11:47
 */

data class Subscriber(
    val id: SubscriberId = SubscriberId.NONE,
    val name: String = "",
    val login: String = "",
    val telegramId: TelegramId = TelegramId.NONE,
    val startSubscribeAt: Instant = Instant.NONE,
    val endSubscribeAt: Instant = Instant.NONE,
    val typeSubscribe: TypeSubscribe = TypeSubscribe.NONE,
    var enable: Boolean = true,
)