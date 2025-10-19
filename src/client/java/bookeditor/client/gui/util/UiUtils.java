package bookeditor.client.gui.util;

public final class UiUtils {
    private UiUtils() {}

    public static String humanReadableBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format("%.1f KB", kb);
        double mb = kb / 1024.0;
        return String.format("%.2f MB", mb);
    }
}

