package pepe.utils;

import java.util.Set;
import java.util.regex.Pattern;

@SuppressWarnings("all")
public class Utils {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{5,32}$");

    private static final Set<String> PEPE_COMMANDS = Set.of(
            "/horo@pepe_wotafa_bot",
            "/settings@pepe_wotafa_bot",
            "/start@pepe_wotafa_bot",
            "/pepe@pepe_wotafa_bot",
            "/info@pepe_wotafa_bot",
            "/surprise@pepe_wotafa_bot"
    );

    public static String decorate(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            double rand = Math.random();
            if (rand < 0.65) {
                sb.append("<b>").append(c).append("</b>");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static boolean isPepeCommand(String text) {
        return PEPE_COMMANDS.contains(text);
    }

    public static String validateUserName(String userName) {
        if (userName.startsWith("@")) {
            userName = userName.substring(1);
        }
        if (!USERNAME_PATTERN.matcher(userName).matches()) {
            return null;
        }
        return userName;
    }
}
