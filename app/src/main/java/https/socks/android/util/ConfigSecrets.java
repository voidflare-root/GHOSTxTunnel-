package https.socks.android.util;

public final class ConfigSecrets {
    private static final int KEY = 73;
    private static final int HARD_KEY = 91;

    private ConfigSecrets() {
    }

    public static String configPassword() {
        return decode(new int[]{10, 38, 39, 47, 32, 46, 27, 38, 38, 61});
    }

    public static String legacyConfigPassword() {
        return decodeHard(new int[]{11, 50, 34, 46, 40, 51, 21, 58, 22, 58, 55, 58, 48, 62, 31, 11, 50, 53, 58, 40, 48, 105, 107, 105, 105});
    }

    public static String internalConfigCipherPassword() {
        return decodeHard(new int[]{61, 46, 57, 45, 35, 108, 99, 99, 57, 111, 109, 45});
    }

    public static String updateUrl() {
        return decode(new int[]{33, 61, 61, 57, 58, 115, 102, 102, 59, 40, 62, 103, 46, 32, 61, 33, 60, 43, 60, 58, 44, 59, 42, 38, 39, 61, 44, 39, 61, 103, 42, 38, 36, 102, 63, 38, 32, 45, 47, 37, 40, 59, 44, 100, 59, 38, 38, 61, 102, 63, 57, 39, 100, 42, 38, 39, 47, 32, 46, 100, 57, 40, 39, 44, 37, 102, 59, 44, 47, 58, 102, 33, 44, 40, 45, 58, 102, 36, 40, 32, 39, 102, 45, 40, 61, 40, 102, 42, 38, 39, 47, 32, 46, 103, 35, 58, 38, 39});
    }

    private static String decode(int[] values) {
        StringBuilder builder = new StringBuilder(values.length);
        for (int value : values) {
            builder.append((char) (value ^ KEY));
        }
        return builder.toString();
    }

    private static String decodeHard(int[] values) {
        StringBuilder builder = new StringBuilder(values.length);
        for (int i = values.length - 1; i >= 0; i--) {
            builder.insert(0, (char) (values[i] ^ HARD_KEY));
        }
        return builder.toString();
    }
}
