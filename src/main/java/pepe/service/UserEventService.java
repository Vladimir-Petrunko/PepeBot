package pepe.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import pepe.entity.UserEvent;
import pepe.repository.UserEventRepository;
import pepe.utils.DateUtils;
import pepe.utils.Utils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@SuppressWarnings("all")
public class UserEventService {
    private final UserEventRepository userEventRepository;

    public boolean containsUserIdExceptInnerId(Long userId, String innerId) {
        return userEventRepository.countByUserIdAndTypeAndInnerType(userId, "INLINE", "+") > 0;
    }

    public List<Long> findInactiveUsers() {
        return userEventRepository.findInactiveUsers();
    }

    public Long getUserId(String userName) {
        return userEventRepository.getUserId(userName);
    }

    public Long getUserName(Long userId) {
        return userEventRepository.getUserName(userId);
    }

    public void save(UserEvent update) {
        userEventRepository.save(update);
    }

    public void saveUpdate(Update update, String type, String innerType) {
        Long userId = getUserId(update);
        Long chatId = getChatId(update);
        String message = Optional.ofNullable(update.getMessage()).map(Message::getText).orElse("");
        if (update.hasInlineQuery()) {
            message = update.getInlineQuery().getQuery();
        }

        // Ignore ordinary messages in groups with bot
        if (!Objects.equals(chatId, userId) && !Utils.isPepeCommand(message) && type.equals("BASIC")) {
            return;
        }

        UserEvent userEvent = new UserEvent();
        userEvent.setUserId(userId);
        userEvent.setChatId(chatId);
        userEvent.setUserName(getUserName(update));
        userEvent.setChatName(getChatName(update));
        userEvent.setTimestamp(DateUtils.getCurrentLocalDateTime());
        userEvent.setMessageLength(message.length());
        if (update.hasInlineQuery()) {
            userEvent.setInnerId(update.getInlineQuery().getId());
        }
        userEvent.setType(type);
        userEvent.setInnerType(innerType);
        userEventRepository.save(userEvent);
    }

    public static User getFrom(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom();
        } else if (update.hasInlineQuery()) {
            return update.getInlineQuery().getFrom();
        } else {
            return update.getMessage().getFrom();
        }
    }

    public static Chat getChat(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChat();
        } else if (update.hasInlineQuery()) {
            return null;
        } else {
            return update.getMessage().getChat();
        }
    }

    public static Long getUserId(Update update) {
        return Optional.ofNullable(getFrom(update)).map(User::getId).orElse(null);
    }

    public static Long getChatId(Update update) {
        return Optional.ofNullable(getChat(update)).map(Chat::getId).orElse(null);
    }

    public static String getUserName(Update update) {
        return Optional.ofNullable(getFrom(update)).map(User::getUserName).orElse(null);
    }

    public static String getChatName(Update update) {
        return Optional.ofNullable(getChat(update)).map(Chat::getTitle).orElse(null);
    }

    public static String getMessage(Update update) {
        String message = null;
        if (update.hasCallbackQuery()) {
            message = update.getCallbackQuery().getData();
        } else if (update.hasInlineQuery()) {
            message = update.getInlineQuery().getQuery();
        } else {
            message = update.getMessage().getText();
        }
        return Objects.isNull(message) ? "" : message;
    }
}
