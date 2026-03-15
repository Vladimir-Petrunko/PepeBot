package pepe;

import com.github.demidko.aot.WordformMeaning;
import com.github.demidko.aot.morphology.PartOfSpeech;
import org.springframework.data.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.github.demidko.aot.WordformMeaning.lookupForMeanings;

@SuppressWarnings("all")
public class Library {
    private static final String[] PEPE = new String[]{"пэпэ", "пэпэ шнэйне", "ко-ко шнэйне", "ко-ко шнейне", "кхе-кхе", "кхе-кхе", "ыйее"};
    private static final String[] SHN = new String[]{"шнейне", "шнэйне"};
    private static final String[] WTF = new String[]{"ватафа", "ватафа", "вотафа", "вотафа", "втфа", "фа", "фа"};
    private static final String ALPHANUMERIC_REGEX = "[а-яА-Я0-9ёЁ]+";
    private static final String NUMERIC_REGEX = "[0-9]+";
    private static final Pattern PUNCTUATION_REGEX = Pattern.compile("\\p{Punct}");
    private static final HashMap<String, Boolean> good = new HashMap<>();
    private static final Map<String, Map<String, Double>> rates = Map.of(
            "pepe", Map.of("pepe-small", 0.2, "pepe-medium", 0.35, "pepe-big", 0.5, "pepe-xl", 0.65),
            "wtf-s", Map.of("pepe-small", 0.2, "pepe-medium", 0.35, "pepe-big", 0.5, "pepe-xl", 0.65),
            "good", Map.of("pepe-small", 0.1, "pepe-medium", 0.2, "pepe-big", 0.3, "pepe-xl", 0.45),
            "a", Map.of("pepe-small", 0.2, "pepe-medium", 0.4, "pepe-big", 0.6, "pepe-xl", 0.75),
            "pepe-wtf", Map.of("pepe-small", 0.1, "pepe-medium", 0.25, "pepe-big", 0.4, "pepe-xl", 0.55),
            "shn-wtf", Map.of("pepe-small", 0.1, "pepe-medium", 0.2, "pepe-big", 0.4, "pepe-xl", 0.55)
    );
    private static final String[] RAND = new String[]{
            "Фа!", "Пэпэ!", "Ватафа!", "Вотафа!", "Втфа!", "Слава Пэпэ!"
    };

    public static String getRandom() {
        int i = (int)(Math.random() * RAND.length);
        return RAND[i];
    }

    public static String getRandomStart() {
        int i = (int)(Math.random() * (RAND.length - 1));
        return RAND[i];
    }

    public static boolean isPunctuation(char c) {
        return PUNCTUATION_REGEX.matcher(String.valueOf(c)).matches();
    }

    public static boolean within(double threshold) {
        return Math.random() < threshold;
    }

    private static String generateWtfSentence(boolean uppercase, String pepe) {
        String begin = generateWtf(pepe);
        if (uppercase) {
            begin = capitalize(begin);
        }
        if (within(getRate("shn-wtf", pepe))) {
            begin += " " + generateShn() + " " + generateWtf(pepe);
        }
        return begin;
    }

    private static String generateShn() {
        int i = (int) (Math.random() * SHN.length);
        return SHN[i];
    }

    private static String generateWtf(String pepe) {
        int i = (int) (Math.random() * WTF.length);
        String result = WTF[i];
        while (within(getRate("a", pepe))) {
            result += 'а';
        }
        return result;
    }

    private static String generatePepe(String pepe) {
        int i = (int) (Math.random() * PEPE.length);
        if (!PEPE[i].equals("кхе-кхе") && !PEPE[i].equals("ыйее") && within(getRate("pepe-wtf", pepe))) {
            return PEPE[i] + " " + generateWtf(pepe);
        }
        if (PEPE[i].equals("ыйее")) {
            String s = PEPE[i];
            while (within(0.5)) {
                s += "е";
            }
            return s;
        }
        return PEPE[i];
    }

    private static boolean isAlphanumeric(String s) {
        if (s == null) {
            return true;
        }
        return s.matches(ALPHANUMERIC_REGEX);
    }

    private static boolean isNumeric(String s) {
        if (s == null) {
            return true;
        }
        return s.matches(NUMERIC_REGEX);
    }

    private static double getRate(String type, String pepe) {
        return rates.get(type).get(pepe);
    }

    public static Pair<String, Boolean> change(String prev, String curr, String next, double capitalizationRate, String pepe) {
        if (next == null) {
            String s = generateWtfSentence(within(capitalizationRate), pepe);
            if (isPunctuation(curr.charAt(curr.length() - 1))) {
                return Pair.of(curr + " " + s + curr, false);
            } else {
                return Pair.of(curr + ". " + s + ".", false);
            }
        }
        if (curr.equals(",")) {
            if (within(getRate("pepe", pepe))) {
                return Pair.of(", " + generatePepe(pepe) + ",", true);
            } else {
                return Pair.of(",", false);
            }
        } else if (curr.equals(".") || curr.equals("!") || curr.equals("?")) {
            if (isAlphanumeric(prev) && isAlphanumeric(next)) {
                if (within(getRate("wtf-s", pepe))) {
                    String s = generateWtfSentence(within(capitalizationRate), pepe);
                    return Pair.of(curr + " " + s + curr, true);
                }
            }
        } else if (isGood(curr) || isNumeric(curr)) {
            if (within(getRate("good", pepe))) {
                String s = curr + ", " + generatePepe(pepe);
                if (next != null && isAlphanumeric(next)) {
                    s += ",";
                }
                return Pair.of(s, true);
            }
        }
        return Pair.of(curr, false);
    }

    public static double getCapitalizationRate(String[] split) {
        int uppercase = 0;
        int lowercase = 0;
        for (int i = 0; i < split.length - 1; i++) {
            if (split[i].equals(".")) {
                if (uppercaseStarts(go(split, i, 1))) {
                    uppercase++;
                } else if (lowercaseStarts(go(split, i, 1))) {
                    lowercase++;
                }
            }
        }
        if (lowercaseStarts(split[0])) {
            lowercase++;
        } else if (uppercaseStarts(split[0])) {
            uppercase++;
        }
        if (uppercase == 0 && lowercase == 0) {
            return 1.0;
        }
        return uppercase / (double)(uppercase + lowercase);
    }

    public static boolean uppercaseStarts(String s) {
        char c = s.charAt(0);
        return c >= 'А' && c <= 'Я';
    }

    public static boolean lowercaseStarts(String s) {
        char c = s.charAt(0);
        return c >= 'а' && c <= 'я';
    }

    public static String capitalize(String s) {
        return capitalize(s.charAt(0)) + s.substring(1);
    }

    private static char capitalize(char c) {
        if (c == 'п') return 'П';
        if (c == 'ш') return 'Ш';
        if (c == 'в') return 'В';
        if (c == 'ф') return 'Ф';
        return c;
    }

    public static String go(String[] split, int i, int dir) {
        i += dir;
        while (i >= 0 && i < split.length) {
            if (!split[i].isBlank()) {
                return split[i];
            }
            i += dir;
        }
        return null;
    }

    private static boolean isGood(String s) {
        if (good.containsKey(s)) {
            return good.get(s);
        }
        List<WordformMeaning> result = lookupForMeanings(s);
        for (WordformMeaning meaning : result) {
            if (meaning.getPartOfSpeech() == PartOfSpeech.Noun
                    || meaning.getPartOfSpeech() == PartOfSpeech.Verb
                    || meaning.getPartOfSpeech() == PartOfSpeech.Pronoun
                    || meaning.getPartOfSpeech() == PartOfSpeech.PronounPredicative
                    || meaning.getPartOfSpeech() == PartOfSpeech.Adjective) {
                good.put(s, true);
                return true;
            }
        }
        good.put(s, false);
        return false;
    }
}
