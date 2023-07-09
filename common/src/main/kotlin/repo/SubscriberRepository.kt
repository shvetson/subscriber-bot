package ru.shvets.subscriber.bot.common.repo

import ru.shvets.subscriber.bot.common.model.Subscriber
import ru.shvets.subscriber.bot.common.model.SubscriberId
import ru.shvets.subscriber.bot.common.model.TelegramId

/**
 * @author  Oleg Shvets
 * @version 1.0
 * @date  23.06.2023 20:58
 */

interface SubscriberRepository {

    suspend fun create(subscriber: Subscriber): Subscriber
    suspend fun read(telegramId: TelegramId): Subscriber?
    suspend fun update(subscriber: Subscriber)
    suspend fun delete(subscriberId: SubscriberId): Boolean
    suspend fun search(subscriber: Subscriber? = null): List<Subscriber>
}