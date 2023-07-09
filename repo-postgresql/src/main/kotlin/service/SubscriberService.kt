package ru.shvets.subscriber.bot.repo.postgresql.service

import com.benasher44.uuid.uuid4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import ru.shvets.subscriber.bot.common.model.Subscriber
import ru.shvets.subscriber.bot.common.model.SubscriberId
import ru.shvets.subscriber.bot.common.model.TelegramId
import ru.shvets.subscriber.bot.common.repo.SubscriberRepository
import ru.shvets.subscriber.bot.log.Logger
import ru.shvets.subscriber.bot.repo.postgresql.entity.SubscriberTable
import ru.shvets.subscriber.bot.repo.postgresql.entity.SubscriberTable.fromRow

/**
 * @author  Oleg Shvets
 * @version 1.0
 * @date  23.06.2023 21:32
 */

class SubscriberService(
    private val randomUuid: () -> String = { uuid4().toString() },
) : SubscriberRepository, Logger {

    override suspend fun create(subscriber: Subscriber): Subscriber = dbQuery {
        SubscriberTable.insert { toRow(it, subscriber, randomUuid) }
            .resultedValues?.singleOrNull()?.let(::fromRow) ?: throw NoSuchElementException()
    }

    override suspend fun read(telegramId: TelegramId): Subscriber? = dbQuery {
        SubscriberTable.select { SubscriberTable.id eq telegramId.asString() }
            .map(::fromRow)
            .singleOrNull()
    }

    override suspend fun update(subscriber: Subscriber) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(subscriberId: SubscriberId): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun search(subscriber: Subscriber?): List<Subscriber> = dbQuery {
        val result = SubscriberTable.selectAll()
        subscriber?.telegramId?.let { result.andWhere { SubscriberTable.telegramId eq subscriber.telegramId.asString() } }
        subscriber?.typeSubscribe?.let { result.andWhere { SubscriberTable.typeSubscribe eq subscriber.typeSubscribe.name } }
        result.orderBy(SubscriberTable.id).map(::fromRow)
    }

    private suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction {
//                addLogger(StdOutSqlLogger)
                block()
            }
        }
}