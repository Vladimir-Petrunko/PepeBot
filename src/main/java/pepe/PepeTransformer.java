package pepe;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import pepe.service.UserSettingsService;

@Component
@RequiredArgsConstructor
public class PepeTransformer {
    private final UserSettingsService userSettingsService;

    public String transform(Long userId, String input) {
        return transform(userId, input, false);
    }

    public String transform(Long userId, String input, boolean horo) {
        String[] split = input.split("(?<=\\s|\\p{Punct})|(?=\\s|\\p{Punct})");
        double capitalizationRate = Library.getCapitalizationRate(split);
        StringBuilder output = new StringBuilder();
        String pepe = horo ? "pepe-small" : userSettingsService.getPepe(userId);
        String textCase = horo ? "case-normal" : userSettingsService.getTextCase(userId);
        String punctuation = horo ? "punctuation-on" : userSettingsService.getPunctuation(userId);
        boolean hasPepeChanged = false;
        for (int i = 0; i < split.length; i++) {
            Pair<String, Boolean> result = Library.change(
                    Library.go(split, i, -1),
                    split[i],
                    Library.go(split, i, 1),
                    capitalizationRate,
                    pepe
            );
            hasPepeChanged |= result.getSecond();
            output.append(result.getFirst());
        }
        String result = output.toString();
        if (!hasPepeChanged) {
            String start = Library.getRandomStart();
            if (!Library.within(capitalizationRate)) {
                start = start.toLowerCase();
            }
            result = start + " " + result;
        }
        if (textCase.equals("case-upper")) {
            result = result.toUpperCase();
        } else if (textCase.equals("case-lower")) {
            result = result.toLowerCase();
        }

        if (punctuation.equals("punctuation-off")) {
            result = result.replaceAll("\\p{Punct}", "");
        }
        return result;
    }
}
