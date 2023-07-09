package ru.shvets.subscriber.bot.log

import org.slf4j.Logger // вручную указать импортируемую библиотеку
import org.slf4j.LoggerFactory

/**
 * @author  Oleg Shvets
 * @version 1.0
 * @date  20.06.2023 19:18
 */

interface Logger {
    val log: Logger
        get() = LoggerFactory.getLogger(this::class.java)
}