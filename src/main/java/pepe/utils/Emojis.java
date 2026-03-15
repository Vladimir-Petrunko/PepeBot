package pepe.utils;

public class Emojis {
    public static final String[] emojis = new String[]{
            "\uD83E\uDD13",
            "\uD83D\uDE0E",
            "\uD83D\uDE07",
            "\uD83D\uDE31",
            "\uD83D\uDE08",
            "\uD83E\uDD17",
            "\uD83D\uDDE3",
            "⚡\uFE0F",
            "\uD83D\uDCA5",
            "\uD83D\uDCB8",
            "\uD83C\uDF88",
            "\uD83D\uDE05",
            "\uD83E\uDD2D",
            "\uD83E\uDD24",
            "\uD83C\uDF83",
            "\uD83E\uDD1F",
            "\uD83D\uDC59"
    };

    public static String getEmoji() {
        int i = (int) (Math.random() * emojis.length);
        return emojis[i];
    }

}
