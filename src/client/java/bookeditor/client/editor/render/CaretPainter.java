package bookeditor.client.editor.render;

import net.minecraft.client.gui.DrawContext;

public class CaretPainter {
    private long lastBlink = 0;
    private boolean blinkOn = true;

    public void renderCaret(DrawContext ctx,
                            boolean isFocused,
                            boolean editable,
                            boolean brushMode,
                            boolean hasSelection,
                            int caretLogicalX,
                            int caretLogicalY,
                            int startScreenX,
                            int startScreenY,
                            double scale,
                            int fontHeight) {
        long now = System.currentTimeMillis();
        if (now - lastBlink > 500) { blinkOn = !blinkOn; lastBlink = now; }
        if (isFocused && editable && !brushMode && blinkOn && !hasSelection) {
            int caretW = Math.max(1, (int)Math.round(scale));
            int caretH = Math.max(1, (int)Math.round(scale * (fontHeight + 2)));
            int caretSX = startScreenX + (int)Math.round(scale * caretLogicalX);
            int caretSY = startScreenY + (int)Math.round(scale * caretLogicalY);
            ctx.fill(caretSX, caretSY, caretSX + caretW, caretSY + caretH, 0xFF000000);
        }
    }

    public void reset() {
        blinkOn = true;
        lastBlink = System.currentTimeMillis();
    }
}