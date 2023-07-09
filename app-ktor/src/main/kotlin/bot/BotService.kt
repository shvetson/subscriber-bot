package ru.shvets.subscriber.bot.app.ktor.bot

import com.vdurmont.emoji.EmojiParser
import io.kotest.common.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.groupadministration.ExportChatInviteLink
import org.telegram.telegrambots.meta.api.methods.groupadministration.KickChatMember
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import ru.shvets.subscriber.bot.app.ktor.config.AppSettings
import ru.shvets.subscriber.bot.common.helper.*
import ru.shvets.subscriber.bot.common.model.*
import ru.shvets.subscriber.bot.common.repo.SubscriberRepository
import ru.shvets.subscriber.bot.log.Logger
import ru.shvets.subscriber.bot.repo.postgresql.service.SubscriberService
import java.time.format.TextStyle
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.Collectors.groupingBy
import kotlin.time.Duration.Companion.days

/**
 * @author  Oleg Shvets
 * @version 1.0
 * @date  20.06.2023 11:55
 */

class BotService(appSettings: AppSettings) : TelegramLongPollingBot(), Logger {
    private val telegramConfig = appSettings.bot
    private val subscriberService = appSettings.repo?.find { it is SubscriberRepository } as SubscriberService

    private val botOwner = telegramConfig.botOwner
    private val privateChannelId = telegramConfig.privateChannel

    init {
        configureMenu()

//        val timer: Timer = Timer()
//        timer.schedule(object : TimerTask() {
//            override fun run() {
//                Runnable { runBlocking { sendAds() } }.run()
//            }
//        }, 60 * 1000, appSettings.timerMinutes * 60 * 1000)
    }

    override fun getBotToken() = telegramConfig.botToken
    override fun getBotUsername() = telegramConfig.botName

    override fun onUpdateReceived(update: Update?) {
        if (update?.hasMessage() == true && update.message?.hasText() == true) {

            val text = update.message.text
            val chatId = update.message.chat.id

            val patternTimeCommand: Pattern = Pattern.compile("^message\\splease$")
            val patternClear: Pattern = Pattern.compile("^clear-expired$")
            val patternGiveRights: Pattern = Pattern.compile("^give-rights\\s(\\d*)$")

            val matcherTimeCommand: Matcher = patternTimeCommand.matcher(text)
            val matcherClear = patternClear.matcher(text)
            val matcherGiveRights = patternGiveRights.matcher(text)

            if (matcherTimeCommand.find()) {
                sendInfoSupport(
                    "Пользователь запросил полный доступ:\n" +
                            "\nLogin: @${update.message.from.userName}" +
                            "\nName: ${update.message.from.firstName} ${update.message.from.lastName}" +
                            "\nChat ID: [${chatId}](${chatId})"
                )

                val messageSuccess = SendMessage()
                messageSuccess.text = "Ваши данные получены, идет проверка."
                messageSuccess.chatId = chatId.toString()
                execute(messageSuccess)

            } else if (matcherClear.find() && isAdmin(chatId.toString())) {
                clearExpired()
            } else if (matcherGiveRights.find() && isAdmin(chatId.toString())) {
                giveRights(matcherGiveRights.group(1))
            } else {
                val message = getCommandResponse(
                    text,
                    update.message.from,
                    chatId.toString()
                )

                message.enableHtml(true)
                message.parseMode = ParseMode.MARKDOWN
                message.chatId = chatId.toString()
                execute(message)
            }

        } else if (update?.hasCallbackQuery() == true) {

            val message = getCommandResponse(
                update.callbackQuery.data,
                update.callbackQuery.from,
                update.callbackQuery.message.chat.id.toString()
            )

            message.enableHtml(true)
            message.parseMode = ParseMode.MARKDOWN
            message.chatId = update.callbackQuery.message.chat.id.toString()
            execute(message)
        }
    }

    private fun getCommandResponse(text: String, user: User, chatId: String): SendMessage {
        return when (text) {
            Commands.START.command -> {
                handleStartCommand(user.firstName)
            }

            Commands.INFO.command -> {
                handleInfoCommand()
            }

            Commands.DEMO.command -> {
                handleDemoCommand(user.userName, user.id.toString(), user.firstName, chatId)
            }

            Commands.ACCESS.command -> {
                handleAccessCommand()
            }

            Commands.SUCCESS.command -> {
                handleSuccessCommand()
            }

            else -> {
                handleNotFoundCommand()
            }
        }
    }

    private fun handleNotFoundCommand(): SendMessage {
        return SendMessage().apply {
            text = "Вы что-то сделали не так. *Выберите команду*"
            replyMarkup = getKeyBoard()
        }
    }

    private fun handleStartCommand(username: String): SendMessage {
        return SendMessage().apply {
            text = EmojiParser
                .parseToUnicode("Привет, _${username}_! :wave: Я - Telegram bot :blush: \n*Доступные команды:*")
            replyMarkup = getKeyBoard()
        }
    }

    private fun handleInfoCommand(): SendMessage {
        return SendMessage().apply {
            text = "Этот канал о ...!"
            replyMarkup = getKeyBoard()
        }
    }

    private fun handleDemoCommand(username: String, id: String, name: String, chatId: String): SendMessage {
        val message = SendMessage()
        if (isDemoAccess(chatId)) {
            message.text = "Вы уже получали демо-доступ"
        } else {
            message.text =
                "Ссылка для доступа к закрытому каналу: ${getChatInviteLink()} \nЧерез 3 дня Вы будете исключены из канала"
            addInfoSubscriberToDb(username, chatId, name, TypeSubscribe.DEMO)
        }
        message.replyMarkup = getKeyBoard()
        return message
    }

    private fun handleAccessCommand(): SendMessage {
        return SendMessage().apply {
            text = "Чтобы получить полный доступ, Вам надо сказать волшебное слово"
            replyMarkup = getKeyBoard()
        }
    }

    private fun handleSuccessCommand(): SendMessage {
        return SendMessage().apply {
            text = "После проверки Вам выдадут полный доступ"
            replyMarkup = getKeyBoard()
        }
    }

    private fun getChatInviteLink(): String {
        val exportChatInviteLink = ExportChatInviteLink()
        exportChatInviteLink.chatId = privateChannelId
        return execute(exportChatInviteLink)
    }

    private fun addInfoSubscriberToDb(
        username: String?,
        chatId: String,
        name: String?,
        typeSubscribe: TypeSubscribe,
    ): Subscriber {

        val subscriber = Subscriber(
            name = name ?: "",
            login = username ?: "",
            telegramId = TelegramId(chatId),
            startSubscribeAt = Clock.System.now(),
            endSubscribeAt = if (typeSubscribe == TypeSubscribe.DEMO) Clock.System.now()
                .plus(3.days) else Clock.System.now().plus(30.days),
            typeSubscribe = typeSubscribe,
        )
        return runBlocking { subscriberService.create(subscriber) }
    }

    private fun isDemoAccess(chatId: String): Boolean {
        val subscriber = Subscriber(telegramId = TelegramId(chatId), typeSubscribe = TypeSubscribe.DEMO)
        val result = runBlocking { subscriberService.search(subscriber) }.firstOrNull()
        return result != null
    }

    private fun sendInfoSupport(message: String) {
        val messageSupport = SendMessage()
        messageSupport.text = message
        messageSupport.chatId = botOwner.toString()
        execute(messageSupport)
    }

    private fun isAdmin(chatId: String): Boolean {
        return chatId.toLong() == botOwner
    }

    private fun clearExpired() {
        val subscribers: List<Subscriber> = runBlocking { subscriberService.search() }
        val successDeleted: MutableList<Subscriber> = ArrayList()

        for (subscriber in subscribers) {

            val kickChatMember = KickChatMember() // работает с версией по 5.2, следующие - deprecated
            kickChatMember.chatId = privateChannelId
            kickChatMember.userId = subscriber.telegramId.asString().toLong()
            execute(kickChatMember)

            disableSubscriber(subscriber.telegramId.asString())

            val message = SendMessage()
            message.text = "Ваш доступ к каналу окончен"
            message.chatId = subscriber.telegramId.asString()
            execute(message)

            successDeleted.add(subscriber)
            Thread.sleep(100)
        }
    }

    private fun disableSubscriber(chatId: String) {
        val subscriber = runBlocking { subscriberService.read(TelegramId(chatId)) }
        subscriber?.apply {
            enable = false
        }

        runBlocking {
            if (subscriber != null) {
                subscriberService.update(subscriber)
            }
        }
    }

    private fun giveRights(chatId: String) {
        val unbanChatMember = UnbanChatMember()
        unbanChatMember.chatId = privateChannelId
        unbanChatMember.onlyIfBanned = false
        unbanChatMember.userId = chatId.toLong()
        execute(unbanChatMember)

        addInfoSubscriberToDb(null, chatId, null, TypeSubscribe.FULL)

        val message = SendMessage()
        message.text = "Вам выдан полный доступ: ${getChatInviteLink()}"
        message.chatId = chatId
        execute(message)
    }

    private fun getStat(): List<MonthStat> {
        val subscriber = Subscriber(typeSubscribe = TypeSubscribe.FULL)
        val data = runBlocking { subscriberService.search(subscriber) }

        val stats: Map<String, List<Subscriber>> = data
            .stream()
            .collect(groupingBy { item ->
                item.startSubscribeAt
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .month.getDisplayName(
                        TextStyle.SHORT, Locale.US
                    )
            })
        return stats.entries.stream().map { entry -> MonthStat(entry.key, entry.value.size) }
            .collect(Collectors.toList())
    }

    private fun getKeyBoard(): InlineKeyboardMarkup {
        val inlineKeyboardMarkup = InlineKeyboardMarkup()

        val inlineKeyboardButton = InlineKeyboardButton()
        inlineKeyboardButton.text = INFO_LABEL
        inlineKeyboardButton.callbackData = Commands.INFO.command

        val inlineKeyboardButtonAccess = InlineKeyboardButton()
        inlineKeyboardButtonAccess.text = ACCESS_LABEL
        inlineKeyboardButtonAccess.callbackData = Commands.ACCESS.command

        val inlineKeyboardButtonDemo = InlineKeyboardButton()
        inlineKeyboardButtonDemo.text = DEMO_LABEL
        inlineKeyboardButtonDemo.callbackData = Commands.DEMO.command

        val inlineKeyboardButtonSuccess = InlineKeyboardButton()
        inlineKeyboardButtonSuccess.text = SUCCESS_LABEL
        inlineKeyboardButtonSuccess.callbackData = Commands.SUCCESS.command

        val keyboardButtons: MutableList<List<InlineKeyboardButton>> = ArrayList()

        val keyboardButtonRows1: MutableList<InlineKeyboardButton> = ArrayList()
        keyboardButtonRows1.add(inlineKeyboardButton)
        keyboardButtonRows1.add(inlineKeyboardButtonAccess)

        val keyboardButtonRows2: MutableList<InlineKeyboardButton> = ArrayList()
        keyboardButtonRows2.add(inlineKeyboardButtonSuccess)

        val keyboardButtonRows3: MutableList<InlineKeyboardButton> = ArrayList()
        keyboardButtonRows3.add(inlineKeyboardButtonDemo)

        keyboardButtons.add(keyboardButtonRows1)
        keyboardButtons.add(keyboardButtonRows2)
        keyboardButtons.add(keyboardButtonRows3)

        inlineKeyboardMarkup.keyboard = keyboardButtons

        return inlineKeyboardMarkup
    }

//    private fun register(chatId: Long) {
//        val sendMessage: SendMessage = SendMessage(chatId.toString(), QUESTION_TEXT)
//
//        val markupInLine: InlineKeyboardMarkup = InlineKeyboardMarkup()
//        val rowsInLine: MutableList<MutableList<InlineKeyboardButton>> = ArrayList()
//
//        val rowInLine: MutableList<InlineKeyboardButton> = ArrayList()
//
//        val yesBtn = InlineKeyboardButton()
//        yesBtn.apply {
//            text = "Yes"
//            callbackData = YES_BUTTON // TODO константа
//        }
//
//        val noBtn = InlineKeyboardButton()
//        noBtn.apply {
//            text = "No"
//            callbackData = NO_BUTTON // TODO константа
//        }
//
//        rowInLine.add(yesBtn)
//        rowInLine.add(noBtn)
//
//        rowsInLine.add(rowInLine)
//        markupInLine.keyboard = rowsInLine
//        sendMessage.replyMarkup = markupInLine
//
//        execute(sendMessage)
//    }

//    private fun registeredUser(message: Message?) {
//        message?.chat?.let {
//            val chatId = it.id.toString()
//            val user = runBlocking { userService.read(ChatId(chatId)) }
//
//            if (user == null) {
//                runBlocking {
//                    userService.create(
//                        User(
//                            chatId = ChatId(chatId),
//                            firstName = it.firstName ?: "",
//                            lastName = it.lastName ?: "",
//                            userName = it.userName ?: "",
//                        )
//                    )
//                }
//            }
//        }
//    }

//    private fun getAll(chatId: Long) {
//        val messageTest = runBlocking { reminderService.getRemindersForChat(chatId) }
//            .joinToString(prefix = "List:\n-> ", separator = "\n-> ") { it.toString() }
//
//        if (messageTest.isNotEmpty()) {
//            sendMessage(messageTest, chatId)
//        }
//    }
//
//    private fun saveReminder(chatId: Long, message: String) {
//        val reminder = parserMessageToReminder(chatId, message)
//        runBlocking { reminderService.create(reminder) }
//        sendMessage("Scheduled '${reminder.description}'", chatId)
//    }
//
//    private fun deleteReminder(chatId: Long, message: String): Boolean {
////        val reminder = parserMessageToReminder(chatId, message)
//        val reminder = runBlocking { reminderService.findByChatIdAndDescriptionAndMinutesAndHours(chatId, message) }
//        val reminderId = reminder?.id?.asString() ?: ""
//        return runBlocking { reminderService.delete(ReminderId(reminderId)) }
//    }

    private fun executeEditMessageText(messageId: Int, chatId: Long, text: String) {
        val message = EditMessageText()
        message.apply {
            this.messageId = messageId
            this.chatId = chatId.toString()
            this.text = text
        }
        execute(message)
    }

    private fun sendMessage(chatId: Long, message: String, keyboardMarkup: ReplyKeyboardMarkup? = null) {
        val sendMessage = SendMessage(chatId.toString(), message)
        sendMessage.replyMarkup = keyboardMarkup
        execute(sendMessage)
    }

    private fun prepareKeyboard(): ReplyKeyboardMarkup {
        val keyboardMarkup: ReplyKeyboardMarkup = ReplyKeyboardMarkup()
        val keyboardRows: MutableList<KeyboardRow> = ArrayList()

        val row1: KeyboardRow = KeyboardRow()
        val row2: KeyboardRow = KeyboardRow()

        // #1
        row1.add("weather")
        row1.add("get random joke")
        keyboardRows.add(row1)

        // #2
        row2.add("register")
        row2.add("check my data")
        row2.add("delete my data")
        keyboardRows.add(row2)

        keyboardMarkup.keyboard = keyboardRows
        return keyboardMarkup
    }

    private fun configureMenu() {
        val listOfCommands: MutableList<BotCommand> = mutableListOf()

        listOfCommands.add(BotCommand(Commands.START.command, START_LABEL))
        listOfCommands.add(BotCommand(Commands.INFO.command, INFO_LABEL))
        listOfCommands.add(BotCommand(Commands.DEMO.command, DEMO_LABEL))
        listOfCommands.add(BotCommand(Commands.ACCESS.command, ACCESS_LABEL))
        listOfCommands.add(BotCommand(Commands.SUCCESS.command, SUCCESS_LABEL))

        execute(SetMyCommands(listOfCommands, BotCommandScopeDefault(), null))
    }

    fun start() {
        TelegramBotsApi(DefaultBotSession::class.java).registerBot(this)
        log.info("Bot started")
    }
}