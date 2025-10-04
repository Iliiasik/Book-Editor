package bookeditor.client.editor.text;

import bookeditor.data.BookData;
import net.minecraft.client.font.TextRenderer;

public final class TextHitTester {
    private TextHitTester() {}

    public static int widthOf(TextRenderer tr, String text, int from, int to, float localScale) {
        int w = 0;
        for (int i = Math.max(0, from); i < Math.min(text.length(), to); i++) {
            char ch = text.charAt(i);
            w += (int)Math.ceil(tr.getWidth(String.valueOf(ch)) * localScale);
        }
        return w;
    }

    public static void placeCaretByApprox(TextRenderer tr, BookData.Page page,
                                          int[] caretNodeOut, int[] caretOffsetOut,
                                          int lx, int ly, int maxTextW) {
        int maxW = Math.max(0, maxTextW);
        int baseLine = tr.fontHeight;
        int targetNode = 0;
        int targetOffset = 0;
        int yCursor = 0;
        int lineX = 0;
        if (page == null) { caretNodeOut[0] = 0; caretOffsetOut[0] = 0; return; }
        for (int i = 0; i < page.nodes.size(); i++) {
            BookData.Node n = page.nodes.get(i);
            if (n instanceof BookData.TextNode tn) {
                String text = tn.text == null ? "" : tn.text;
                float scale = tn.size;
                int ci = 0;
                while (ci <= text.length()) {
                    if (ci == text.length()) break;
                    char ch = text.charAt(ci);
                    if (ch == '\n') {
                        if (approxHit(yCursor, ly, (int)((baseLine + 2) * scale))) { caretNodeOut[0] = i; caretOffsetOut[0] = ci; return; }
                        yCursor += (int)((baseLine + 2) * scale);
                        lineX = 0; ci++; continue;
                    }
                    int charW = (int)(tr.getWidth(String.valueOf(ch)) * scale);
                    if (lineX + charW > maxW) {
                        if (approxHit(yCursor, ly, (int)((baseLine + 2) * scale))) { caretNodeOut[0] = i; caretOffsetOut[0] = ci; return; }
                        yCursor += (int)((baseLine + 2) * scale);
                        lineX = 0;
                    }
                    if (approxHit(yCursor, ly, (int)((baseLine + 2) * scale))) {
                        if (lx < lineX + charW / 2) { caretNodeOut[0] = i; caretOffsetOut[0] = ci; return; }
                        else { caretNodeOut[0] = i; caretOffsetOut[0] = ci + 1; }
                    }
                    lineX += charW; ci++;
                }
                lineX = 0;
            }
            targetNode = i;
            targetOffset = (page.nodes.get(i) instanceof BookData.TextNode t) ? t.text.length() : 1;
        }
        caretNodeOut[0] = targetNode;
        caretOffsetOut[0] = targetOffset;
    }

    private static boolean approxHit(int yTop, int ly, int h) { return ly >= yTop && ly <= yTop + h; }
}