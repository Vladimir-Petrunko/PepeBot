package pepe.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pepe.entity.UserSettings;
import pepe.repository.UserSettingsRepository;

import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@SuppressWarnings("all")
public class UserSettingsService {
    private final UserSettingsRepository userSettingsRepository;

    public static final Map<String, String> PEPES = Map.of(
            "pepe-small", "малое ко-ко шнейне",
            "pepe-medium", "среднее пэпэ шнейне",
            "pepe-big", "большая вотафааа",
            "pepe-xl", "XL пэпэ-шнейнище"
    );

    public static final Map<String, String> TEXT_CASES = Map.of(
            "case-normal", "Оставить как есть",
            "case-upper", "ВСЕМИ ЗАГЛАВНЫМИ",
            "case-lower", "всеми прописными"
    );

    public static final Map<String, String> PUNCTUATION = Map.of(
            "punctuation-on", "оставить",
            "punctuation-off", "убрать"
    );

    public void updateSettings(Long userId, String setting) {
        UserSettings userSettings = userSettingsRepository.findByUserId(userId).orElse(null);

        if (Objects.isNull(userSettings)) {
            userSettings = new UserSettings();
            userSettings.setUserId(userId);
            userSettings.setPepe("pepe-medium");
            userSettings.setTextCase("case-normal");
            userSettings.setPunctuation("punctuation-on");
        }

        if (setting.startsWith("pepe")) {
            userSettings.setPepe(setting);
        } else if (setting.startsWith("case")) {
            userSettings.setTextCase(setting);
        } else {
            userSettings.setPunctuation(setting);
        }

        userSettingsRepository.save(userSettings);
    }

    public String getSettings(Long userId) {
        UserSettings userSettings = userSettingsRepository.findByUserId(userId).orElse(new UserSettings());

        return "Фа! Ты выбрал:\n" +
                "<i>Как много пэпэ: </i>" + PEPES.get(userSettings.getPepe()) + "\n" +
                "<i>Заглавные/прописные в ответе: </i>" + TEXT_CASES.get(userSettings.getTextCase()) + "\n" +
                "<i>Знаки препинания в ответе: </i>" + PUNCTUATION.get(userSettings.getPunctuation());
    }

    public String getPepe(Long userId) {
        return userSettingsRepository.findByUserId(userId).map(UserSettings::getPepe).orElse("pepe-medium");
    }

    public String getTextCase(Long userId) {
        return userSettingsRepository.findByUserId(userId).map(UserSettings::getTextCase).orElse("case-normal");
    }

    public String getPunctuation(Long userId) {
        return userSettingsRepository.findByUserId(userId).map(UserSettings::getPunctuation).orElse("punctuation-on");
    }
}
