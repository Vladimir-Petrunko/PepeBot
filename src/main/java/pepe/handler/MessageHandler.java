package pepe.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pepe.Library;
import pepe.PepeBot;
import pepe.PepeTransformer;
import pepe.entity.UserEvent;
import pepe.entity.UserSurprise;
import pepe.horoscope.HoroscopeFactory;
import pepe.repository.UserBanRepository;
import pepe.service.UserEventService;
import pepe.service.UserSettingsService;
import pepe.service.UserSurpriseService;
import pepe.utils.DateUtils;
import pepe.utils.Emojis;
import pepe.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Log4j2
@Component
@RequiredArgsConstructor
public class MessageHandler {
    private final PepeTransformer pepeTransformer;
    private final HoroscopeFactory horoscopeFactory;
    private final UserSettingsService userSettingsService;
    private final UserEventService userEventService;
    private final UserSurpriseService userSurpriseService;
    private final UserBanRepository userBanRepository;

    public Integer sendMessage(PepeBot bot, Long chatId, String message, InlineKeyboardMarkup markup) {
        return sendMessage(bot, chatId, message, markup, false, null, false);
    }

    public Integer sendMessage(PepeBot bot, Long chatId, String message, InlineKeyboardMarkup markup, boolean admin) {
        return sendMessage(bot, chatId, message, markup, admin, null, false);
    }

    public Integer sendMessage(PepeBot bot, Long chatId, String message, InlineKeyboardMarkup markup, boolean admin, Integer reply, boolean errored) {
        if (userBanRepository.existsByUserId(chatId)) {
            return null;
        }
        if (message.length() > 4000) {
            Integer first = null;
            for (int i = 0; i < message.length(); i += 4000) {
                Integer result = sendMessage(bot, chatId, message.substring(i, Math.min(i + 4000, message.length())), markup, admin);
                if (Objects.isNull(first)) {
                    first = result;
                }
            }
            return first;
        }
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .parseMode("HTML")
                .text(message)
                .build();

        if (Objects.nonNull(reply)) {
            sendMessage.setReplyToMessageId(reply);
        }

        if (Objects.nonNull(markup)) {
            sendMessage.setReplyMarkup(markup);
        }

        try {
            return bot.execute(sendMessage).getMessageId();
        } catch (TelegramApiException e) {
            if (!admin) {
                if (!errored) {
                    sendMessage(bot, chatId, "Сори, не смог обработать сообщение, пэпэ... Вотафа...", null, false, null, true);
                }
                log.error(e);
            }
        }
        return null;
    }

    public Integer sendMessage(PepeBot bot, Long chatId, String message) {
        return sendMessage(bot, chatId, message, null);
    }

    public void sendHoroscope(PepeBot bot, Update update) {
        Long userId = UserEventService.getUserId(update);
        Long chatId = UserEventService.getChatId(update);
        try {
            SendPhoto sendPhoto = horoscopeFactory.getHoroscopePhoto(userId);
            String rest = null;
            if (sendPhoto.getCaption().length() > 1000) {
                rest = sendPhoto.getCaption().substring(800);
                sendPhoto.setCaption(sendPhoto.getCaption().substring(0, 800));
            }
            sendPhoto.setChatId(chatId);
            bot.execute(sendPhoto);
            if (Objects.nonNull(rest)) {
                sendMessage(bot, chatId, rest);
            }
            userEventService.saveUpdate(update, "HORO", "");
        } catch (IOException | TelegramApiException e) {
            log.error(e);
        }
    }

    public void sendSurprise(PepeBot bot, Update update) {
        Long userId = UserEventService.getUserId(update);
        String userName = UserEventService.getUserName(update);
        sendMessage(bot, userId, """
                <b>Фа! Время сюрпризов!</b>
                
                Напиши юзернейм человека, кому ты хочешь отправить сообщение!
                """);
        userEventService.saveUpdate(update, "SURPRISE", "start");
        userSurpriseService.beginSurprise(userId, userName);
    }

    public void sendStartMessage(PepeBot bot, Update update) {
        sendMessage(bot, UserEventService.getUserId(update), """
                Добро пожаловать в Пэпэ вотафа бот!
                
                Без лишних слов – отправь в ответ к этому сообщению <code>/pepe</code> или же просто в переписке со мной что-то напиши!
                
                Также можно:
                – <code>/settings</code>: менять прикольные настроечки сообщений,
                – <code>/horo</code>: сгенерировать Пэпэ-гороскоп на сегодня!
                – <code>/surprise</code>: отправить анонимный (или нет) пэпэ-мэсседж другу!
                
                Развлекайся)
                """);
        userEventService.saveUpdate(update, "START", "");
    }

    public void sendSettings(PepeBot bot, Update update) {
        List<InlineKeyboardMarkup> settingsMarkup = getSettingsMarkup();

        Long userId = UserEventService.getUserId(update);
        sendMessage(bot, userId, "на сколько ТЫ хочешь стать Ганвестом??? вотафааа...");
        sendMessage(bot, userId, "насколько сильно добавлять пэпэ-штуки в ответы:", settingsMarkup.get(0));
        sendMessage(bot, userId, "прикольные стили ответов:", settingsMarkup.get(1));
        sendMessage(bot, userId, "что делать с препинанием:", settingsMarkup.get(2));

        userEventService.saveUpdate(update, "SETTINGS", "");
    }

    public void sendPreSurpriseMessage(PepeBot bot, Long userId) {
        Integer id = sendMessage(bot, userId, "Пэпэ вотафа бот почти готов отправить твоё послание, Пэпэ! Как ты хочешь его отправить?", getSurpriseMarkup());
        UserSurprise userSurprise = new UserSurprise();
        userSurprise.setMessageId(id);
        userSurprise.setUserToId(userId);
        userSurprise.setUserFromName("bot");
        userSurpriseService.save(userSurprise);
    }

    private InlineKeyboardMarkup getSurpriseMarkup() {
        return createMarkup(List.of(
                List.of(createButton("анонимно", "send-anon")),
                List.of(createButton("не анонимно", "send-not-anon")),
                List.of(createButton("я передумал", "send-no"))
        ));
    }

    private List<InlineKeyboardMarkup> getSettingsMarkup() {
        List<InlineKeyboardMarkup> markup = new ArrayList<>();

        markup.add(createMarkup(List.of(
                List.of(createButton("Малое ко-ко шнейне", "pepe-small")),
                List.of(createButton("Среднее пэпэ шнейне", "pepe-medium")),
                List.of(createButton("Большая вотафааа", "pepe-big")),
                List.of(createButton("XL пэпэ-шнейнище", "pepe-xl"))
        )));

        markup.add(createMarkup(List.of(
                List.of(createButton("ВСЕМИ ЗАГЛАВНЫМИ", "case-upper")),
                List.of(createButton("Оставить как есть", "case-normal")),
                List.of(createButton("всеми прописными", "case-lower"))
        )));

        markup.add(createMarkup(List.of(
                List.of(createButton("Оставить знаки препинания", "punctuation-on")),
                List.of(createButton("Убрать знаки препинания", "punctuation-off"))
        )));

        return markup;
    }

    private InlineKeyboardMarkup createMarkup(List<List<InlineKeyboardButton>> buttons) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(buttons);
        return markup;
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        return InlineKeyboardButton.builder().text(text).callbackData(callbackData).build();
    }

    @Transactional
    public void sendSurpriseMessage(PepeBot bot, Long userId, boolean anon) {
        UserSurprise userSurprise = userSurpriseService.getActiveSurprise(userId);
        String message = userSurprise.getMessage();
        if (anon || userSurprise.getUserFromName().isEmpty()) {
            message = "<i>Тебе анонимное сообщение:</i>\n\n" + message;
        } else {
            message = "<i>Тебе сообщение от @" + userSurprise.getUserFromName() + "</i>\n\n" + message;
        }
        message += "\n\n<i>Реплайни на это сообщение, и вышлется ответный пэпэ-мэссэдж твоему поклоннику!</i>";
        if (Objects.isNull(userSurprise.getUserToId())) {
            sendMessage(bot, userId, "Вотафа, не получилось! Может, ты ошибся или твой друг ещё не в боте?");
            userSurpriseService.deleteOngoingSurprises(userId);
            userSurpriseService.deleteOngoingSurprises2(userId);
            return;
        }
        Integer id = sendMessage(bot, userSurprise.getUserToId(), message);
        if (Objects.isNull(id)) {
            sendMessage(bot, userId, "Вотафа, не получилось! Может, ты ошибся или твой друг ещё не в боте?");
            userSurpriseService.deleteOngoingSurprises(userId);
            userSurpriseService.deleteOngoingSurprises2(userId);
            return;
        }
        userSurprise.setSent(1);
        userSurpriseService.save(userSurprise);

        saveReverseSurprise(userSurprise, id, userSurprise.getMessageId());

        sendMessage(bot, userId, "Фа! Готово!");
        userSurpriseService.markCanReply(id);
        sendSurpriseEvent(userId, userSurprise.getUserToId(), false);
    }

    private void saveReverseSurprise(UserSurprise userSurprise, Integer id, Integer reverseId) {
        UserSurprise reverse = new UserSurprise();
        reverse.setMessageId(id);
        reverse.setUserFromId(userSurprise.getUserToId());
        reverse.setUserFromName(userSurprise.getUserToName());
        reverse.setUserToId(userSurprise.getUserFromId());
        reverse.setUserToName(userSurprise.getUserFromName());
        reverse.setReverseMessageId(reverseId);
        reverse.setSent(1);
        userSurpriseService.save(reverse);
    }

    @Transactional
    public void sendReplySurprise(PepeBot bot, Integer orig, UserSurprise userSurprise, String message) {
        UserSurprise reply = new UserSurprise();
        reply.setUserFromId(userSurprise.getUserToId());
        reply.setUserFromName(userSurprise.getUserToName());
        reply.setUserToId(userSurprise.getUserFromId());
        reply.setUserToName(userSurprise.getUserFromName());
        message = pepeTransformer.transform(reply.getUserFromId(), message);
        reply.setMessage(message);

        message = "<i>Тебе ответка!</i>\n\n" + message + "\n\n<i>Реплайни на это сообщение, и вышлется ответный пэпэ-мэссэдж твоему поклоннику!</i>";

        Integer target = userSurprise.getMessageId();

        if (Objects.isNull(reply.getUserToId())) {
            sendMessage(bot, reply.getUserFromId(), "Вотафа, не получилось! Может, ты ошибся или твой друг ещё не в боте?");
            userSurpriseService.deleteOngoingSurprises(reply.getUserFromId());
            userSurpriseService.deleteOngoingSurprises2(reply.getUserFromId());
            return;
        }
        Integer id = sendMessage(bot, reply.getUserToId(), message, null, false, target, false);
        if (Objects.isNull(id)) {
            sendMessage(bot, reply.getUserFromId(), "Вотафа, не получилось! Может, ты ошибся или твой друг ещё не в боте?");
            userSurpriseService.deleteOngoingSurprises(reply.getUserFromId());
            userSurpriseService.deleteOngoingSurprises2(reply.getUserFromId());
            return;
        }
        reply.setSent(1);
        reply.setMessageId(orig);
        userSurpriseService.save(reply);

        saveReverseSurprise(reply, id, reply.getMessageId());

        sendMessage(bot, reply.getUserFromId(), "Фа! Готово!");
        userSurpriseService.markCanReply(id);
        sendSurpriseEvent(reply.getUserFromId(), reply.getUserToId(), true);
    }

    private void sendSurpriseEvent(Long fromId, Long toId, boolean reply) {
        UserEvent userEvent = new UserEvent();
        userEvent.setUserId(fromId);
        userEvent.setTimestamp(DateUtils.getCurrentLocalDateTime());
        userEvent.setType("SURPRISE");
        if (reply) {
            userEvent.setInnerType("reply");
        } else {
            userEvent.setInnerType("normal");
        }
        userEvent.setChatId(toId);
        userEventService.save(userEvent);
    }

    public void clearTrash(PepeBot bot, Long chatId, List<Integer> messages) {
        for (Integer message : messages) {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setMessageId(message);
            deleteMessage.setChatId(chatId);

            try {
                bot.execute(deleteMessage);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Transactional
    public void handleCallback(PepeBot bot, Update update) {
        Long userId = UserEventService.getUserId(update);
        String query = update.getCallbackQuery().getData();
        if (query.startsWith("send")) {
            if (query.equals("send-anon")) {
                sendSurpriseMessage(bot, userId, true);
            } else if (query.equals("send-not-anon")) {
                sendSurpriseMessage(bot, userId, false);
            } else if (query.equals("send-no")) {
                userSurpriseService.deleteOngoingSurprises(userId);
                userSurpriseService.deleteOngoingSurprises2(userId);
                sendMessage(bot, userId, "Фа! Можно и потом)");
            }
        } else {
            userSettingsService.updateSettings(userId, query);
            sendMessage(bot, userId, userSettingsService.getSettings(userId));
            userEventService.saveUpdate(update, "CALLBACK", query);
        }
    }

    public void handleInline(PepeBot bot, Update update) {
        Long userId = UserEventService.getUserId(update);
        String query = update.getInlineQuery().getQuery();
        String inlineQueryId = update.getInlineQuery().getId();
        String transformed = inlineTransform(userId, query);
        String result = transformed;
        if (StringUtils.isEmpty(query)) {
            result = Library.getRandom();
        }

        AnswerInlineQuery answer = AnswerInlineQuery.builder()
                .inlineQueryId(inlineQueryId)
                .results(List.of(
                        InlineQueryResultArticle.builder()
                                .id(inlineQueryId)
                                .title(transformed)
                                .description("напиши как Пэпэ, вотафа!")
                                .inputMessageContent(InputTextMessageContent.builder().messageText(result).parseMode("HTML").build())
                                .build()
                ))
                .cacheTime(1)
                .isPersonal(true)
                .build();

        try {
            bot.execute(answer);
            userEventService.saveUpdate(update, "INLINE", "");
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }

    private String inlineTransform(Long userId, String query) {
        if (StringUtils.isEmpty(query)) {
            return "начни писать сообщение...";
        } else {
            return pepeTransformer.transform(userId, query);
        }
    }

    public void sendInfo(PepeBot bot, Update update) {
        String info = """
                        <b>ТЕПЕРЬ ТЫ ГАНВЕСТ, ПЭПЭ!</b>
                        <i>К а ж д о е, пэпэ, сообщение, будто из уст самого Ганвеста!</i>
                        
                        Как обращаться с ботом (нежно, <i>пэпэ</i>, бережно, <i>шнейне</i>):
                        🫧 в лс: пишешь сообщение и тебе его сразу переделают под Ганвеста! Также работает команда <code>/pepe</code> на реплаях.
                        🫧 в чатах: работает inline-режим и реплаи на сообщения командой <code>/pepe</code>
                        
                        Автор концепции и разработчик – @komendazavr.
                        Смело накидывайте новых идей или пишите просто так)
                        
                        P.S. Бот не аффилирован с Ганвестом, а является творчеством фаната:)
                        """;
        Long userId = UserEventService.getUserId(update);
        Long chatId = UserEventService.getUserId(update);
        sendMessage(bot, Objects.isNull(chatId) ? userId : chatId, info);
        userEventService.saveUpdate(update, "INFO", "");
    }

    public void handlePepe(PepeBot bot, Update update) {
        Long userId = UserEventService.getUserId(update);
        Long chatId = UserEventService.getChatId(update);
        Message reply = Optional.of(update)
                .map(Update::getMessage)
                .map(Message::getReplyToMessage)
                .orElse(null);
        if (Objects.isNull(reply)) {
            userEventService.saveUpdate(update, "PEPE", "empty");
            String err = "Пэпэ такое не одобряет. Команда работает только на ответных и не слишком старых сообщениях!";
            sendMessage(bot, Objects.isNull(chatId) ? userId : chatId, pepeTransformer.transform(userId, err));
        } else {
            String text = Objects.isNull(reply.getText())
                    ? reply.getCaption()
                    : reply.getText();
            if (Objects.isNull(text)) {
                userEventService.saveUpdate(update, "PEPE", "error");
                sendMessage(bot, chatId, "Сори, не смог обработать сообщение, пэпэ... Вотафа...");;
                return;
            }
            String s = pepeTransformer.transform(userId, text);
            String e = Emojis.getEmoji();
            sendMessage(bot, reply.getChatId(), e + Utils.decorate(" ГАНВЕСТ ПЕЧАТАЕТ... ") + e + "\n\n" + s);
            userEventService.saveUpdate(update, "PEPE", "reply");
        }
    }
}
