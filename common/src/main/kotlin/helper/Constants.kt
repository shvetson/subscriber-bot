package ru.shvets.subscriber.bot.common.helper

import kotlinx.datetime.Instant

const val START_LABEL = "Приветствие"
const val INFO_LABEL = "О чем канал?"
const val ACCESS_LABEL = "Как получить доступ?"
const val SUCCESS_LABEL = "Дайте полный доступ"
const val DEMO_LABEL = "Хочу демо-доступ на 3 дня"

private val INSTANT_NONE = Instant.fromEpochMilliseconds(Long.MIN_VALUE)
val Instant.Companion.NONE
    get() = INSTANT_NONE