package bookeditor.data;

public final class BookDataUtils {
    private BookDataUtils() {}

    public static final int MAX_TEXT_LEN = 200_000;
    public static final int MAX_SEGMENTS_PER_TEXTBOX = 1024;
    public static final int MAX_NODES_PER_PAGE = 512;
    public static final int MAX_STROKES_PER_PAGE = 1024;

    public static int clampAlign(int a) {
        return Math.max(BookData.ALIGN_LEFT, Math.min(a, BookData.ALIGN_RIGHT));
    }

    public static String safeString(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen);
    }

    public static String safeString(String s) {
        return safeString(s, MAX_TEXT_LEN);
    }

    public static BookData.TextSegment copySegment(BookData.TextSegment seg) {
        if (seg == null) return null;
        BookData.TextSegment copy = new BookData.TextSegment(seg.text, seg.bold, seg.italic, seg.underline, seg.argb, seg.size);
        copy.align = seg.align;
        return copy;
    }

    public static boolean sameStyle(BookData.TextSegment a, BookData.TextSegment b) {
        if (a == null || b == null) return false;
        return a.bold == b.bold && a.italic == b.italic && a.underline == b.underline && a.argb == b.argb && Float.compare(a.size, b.size) == 0 && a.align == b.align;
    }
}
