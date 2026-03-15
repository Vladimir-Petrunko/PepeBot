package pepe;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberBanned;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import pepe.entity.MassMessage;
import pepe.entity.UserBan;
import pepe.entity.UserSurprise;
import pepe.handler.MessageHandler;
import pepe.repository.MassMessageRepository;
import pepe.repository.UserBanRepository;
import pepe.service.UserEventService;
import pepe.service.UserSurpriseService;
import pepe.utils.DateUtils;
import pepe.utils.Emojis;
import pepe.utils.Utils;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("all")
public class PepeBot extends TelegramLongPollingBot {
    private final MessageHandler messageHandler;
    private final PepeTransformer pepeTransformer;
    private final UserEventService userEventService;
    private final UserSurpriseService userSurpriseService;
    private final UserBanRepository userBanRepository;
    private final MassMessageRepository massMessageRepository;

    private static final Long KOMENDAZAVR = 980168646L;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMyChatMember() != null) {
            if (update.getMyChatMember().getNewChatMember() != null && update.getMyChatMember().getNewChatMember() instanceof ChatMemberBanned) {
                UserBan ban = new UserBan();
                ban.setUserId(update.getMyChatMember().getFrom().getId());
                ban.setUserName(update.getMyChatMember().getFrom().getUserName());
                userBanRepository.save(ban);
            }
        }
        if (update.hasCallbackQuery()) {
            Long userId = update.getCallbackQuery().getFrom().getId();
            messageHandler.clearTrash(this, userId, userSurpriseService.findTrash(userId));
            userSurpriseService.clearTrash(update.getCallbackQuery().getFrom().getId());
            messageHandler.handleCallback(this, update);
        } else if (update.hasInlineQuery()) {
            messageHandler.handleInline(this, update);
        } else {
            String text = UserEventService.getMessage(update);
            Long userId = UserEventService.getUserId(update);
            Long chatId = UserEventService.getChatId(update);
            messageHandler.clearTrash(this, userId, userSurpriseService.findTrash(userId));
            userSurpriseService.clearTrash(userId);

            // Ignore ordinary messages in groups with bot
            if (!Objects.equals(chatId, userId) && !Utils.isPepeCommand(text)) {
                return;
            }

            if (text.startsWith("/surprise")) {
                userSurpriseService.deleteOngoingSurprises(userId);
                userSurpriseService.deleteOngoingSurprises2(userId);
                messageHandler.sendSurprise(this, update);
            } else if (text.startsWith("/mass") && userId.equals(KOMENDAZAVR)) {
                String[] split = text.split("#");
                long fromId = Long.parseLong(split[1]);
                long toId = Long.parseLong(split[2]);
                List<MassMessage> massMessages = massMessageRepository.findInRange(fromId, toId);
                System.out.println(massMessages.size());
                for (int i = 0; i < massMessages.size(); i++) {
                    MassMessage massMessage = massMessages.get(i);
                    Integer result = messageHandler.sendMessage(this, massMessage.getUserId(), massMessage.getMessage(), null, true);
                    massMessage.setTimestamp(DateUtils.getCurrentLocalDateTime());
                    massMessage.setSent(1);
                    massMessageRepository.save(massMessage);
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                    if (i % 10 == 0) {
                        messageHandler.sendMessage(this, KOMENDAZAVR, "Отправлено " + i + " из " + massMessages.size(), null, true);
                    }
                }
                messageHandler.sendMessage(this, KOMENDAZAVR, "ну типо всё:)");
            } else if (text.startsWith("/admin") && userId.equals(KOMENDAZAVR)) {
                String[] split = text.split("#");
                if (split.length == 3) {
                    String userName = split[1].trim();
                    String message = split[2].trim();
                    Long targetId = userEventService.getUserId(userName);
                    Integer result = messageHandler.sendMessage(this, targetId, message, null, true);
                    if (result != null) {
                        messageHandler.sendMessage(this, KOMENDAZAVR, "Отправлено успешно!");
                    } else {
                        messageHandler.sendMessage(this, KOMENDAZAVR, "Ээээыыыы что-то не так пошло...");
                    }
                }
            } else if (text.startsWith("/start")) {
                userSurpriseService.deleteOngoingSurprises(userId);
                userSurpriseService.deleteOngoingSurprises2(userId);
                messageHandler.sendStartMessage(this, update);
            } else if (text.startsWith("/horo")) {
                userSurpriseService.deleteOngoingSurprises(userId);
                userSurpriseService.deleteOngoingSurprises2(userId);
                messageHandler.sendHoroscope(this, update);
            } else if (text.startsWith("/settings")) {
                userSurpriseService.deleteOngoingSurprises(userId);
                userSurpriseService.deleteOngoingSurprises2(userId);
                messageHandler.sendSettings(this, update);
            } else if (text.startsWith("/info")) {
                userSurpriseService.deleteOngoingSurprises(userId);
                userSurpriseService.deleteOngoingSurprises2(userId);
                messageHandler.sendInfo(this, update);
            } else if (text.startsWith("/pepe")) {
                userSurpriseService.deleteOngoingSurprises(userId);
                userSurpriseService.deleteOngoingSurprises2(userId);
                messageHandler.handlePepe(this, update);
            } else {
                Message reply = update.getMessage().getReplyToMessage();
                UserSurprise userSurprise = userSurpriseService.getActiveSurprise(userId);
                if (userSurprise != null) {
                    if (Objects.isNull(userSurprise.getUserToName())) {
                        String userName = Utils.validateUserName(text);
                        if (userName == null) {
                            messageHandler.sendMessage(this, userId, "Невалидный формат юзернейма. Попробуй заново. Фа(");
                        } else {
                            userSurprise.setUserToName(userName);
                            userSurpriseService.save(userSurprise);
                            messageHandler.sendMessage(this, userId, "Фа! Теперь напиши сообщение...");
                        }
                    } else {
                        userSurprise.setMessage(text);
                        userSurprise.setMessageId(update.getMessage().getMessageId());
                        messageHandler.sendPreSurpriseMessage(this, userId);
                    }
                    String result = pepeTransformer.transform(userId, text);
                    userSurprise.setMessage(result);
                    userSurpriseService.save(userSurprise);
                } else {
                    UserSurprise replied = (reply == null)
                        ? null
                        : userSurpriseService.findByMessageId(reply.getMessageId());
                    if (replied != null && Objects.equals(replied.getCanReply(), 1)) {
                        if (replied.getReverseMessageId() != null) {
                            replied = userSurpriseService.findByMessageId(replied.getReverseMessageId());
                        }
                        messageHandler.sendReplySurprise(this, update.getMessage().getMessageId(), replied, text);
                    } else {
                        String result = pepeTransformer.transform(userId, text);
                        String e = Emojis.getEmoji();
                        messageHandler.sendMessage(this, userId, e + Utils.decorate(" ГАНВЕСТ ПЕЧАТАЕТ... ") + e + "\n\n" + result);
                        userEventService.saveUpdate(update, "BASIC", "");
                    }
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "сделайте плиз новый юзернейм";
    }

    @Override
    public String getBotToken() {
        return "и токен тоже там сделайте";
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(PepeBot bot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(new PepeBot(messageHandler, pepeTransformer, userEventService, userSurpriseService, userBanRepository, massMessageRepository));
        return api;
    }
}
