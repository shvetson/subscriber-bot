package ru.shvets.subscriber.bot.repo.postgresql.entity

import kotlinx.datetime.*
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ru.shvets.subscriber.bot.common.helper.NONE
import ru.shvets.subscriber.bot.common.model.*
import java.time.LocalDateTime

/**
 * @author  Oleg Shvets
 * @version 1.0
 * @date  20.06.2023 10:26
 */

object SubscriberTable : Table(name = "subscribers") {
    val id: Column<String> = varchar("id", 128).uniqueIndex()
    val name: Column<String> = varchar("name", 128)
    val login: Column<String> = varchar("login", 64)
    val telegramId: Column<String> = varchar("telegram_id", 128)
    val startSubscribeAt: Column<LocalDateTime> = datetime("start_subscribe_at")
    val endSubscribeAt: Column<LocalDateTime> = datetime("end_subscribe_at")
    val typeSubscribe: Column<String> = varchar("type_subscribe", 24)
    val enable: Column<Boolean> = bool("enable").default(true)

    override val primaryKey: PrimaryKey = PrimaryKey(id)

    fun toRow(it: UpdateBuilder<*>, subscriber: Subscriber, randomUuid: () -> String) {
        it[id] = subscriber.id.takeIf { it != SubscriberId.NONE }?.asString() ?: randomUuid()
        it[name] = subscriber.name.takeIf { it.isNotBlank() } ?: ""
        it[login] = subscriber.login.takeIf { it.isNotBlank() } ?: ""
        it[telegramId] = subscriber.telegramId.takeIf { it != TelegramId.NONE }?.asString() ?: ""
        it[startSubscribeAt] =
            (subscriber.startSubscribeAt.takeIf { it != Instant.NONE }
                ?: Clock.System.now()).toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
        it[endSubscribeAt] =
            (subscriber.endSubscribeAt.takeIf { it != Instant.NONE }
                ?: Clock.System.now()).toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
        it[typeSubscribe] = subscriber.typeSubscribe.takeIf { it != TypeSubscribe.NONE }?.name ?: ""
        it[enable] = subscriber.enable
    }

    fun fromRow(result: ResultRow): Subscriber =
        Subscriber(
            id = SubscriberId(result[id].toString()),
            name = result[name],
            login = result[login],
            telegramId = TelegramId(result[telegramId].toString()),
            startSubscribeAt = result[startSubscribeAt].toKotlinLocalDateTime()
                .toInstant(TimeZone.currentSystemDefault()),
            endSubscribeAt = result[endSubscribeAt].toKotlinLocalDateTime().toInstant(TimeZone.currentSystemDefault()),
            typeSubscribe = TypeSubscribe.valueOf(result[typeSubscribe]),
            enable = result[enable]
        )
}