package bookeditor.client.editor.render;

import net.minecraft.client.gui.DrawContext;

public class CaretPainter {
    private long lastBlink = 0;
    private boolean visible = true;
    private static final long BLINK_INTERVAL = 530;

    public void reset() {
        lastBlink = System.currentTimeMillis();
        visible = true;
    }

    public void renderCaret(DrawContext ctx, boolean focused, boolean editable, boolean brushMode,
                            boolean hasSelection, int caretX, int caretY, int baseScreenX, int baseScreenY,
                            double scale, int baseFontHeight, float textSize) {
        if (!focused || !editable || brushMode || hasSelection) return;

        long now = System.currentTimeMillis();
        if (now - lastBlink > BLINK_INTERVAL) {
            visible = !visible;
            lastBlink = now;
        }

        if (visible) {
            int sx = baseScreenX + (int) Math.round(scale * caretX);
            int sy = baseScreenY + (int) Math.round(scale * caretY);
            int caretHeight = (int) Math.round(scale * baseFontHeight * 1.2f * textSize);
            ctx.fill(sx, sy, sx + 1, sy + caretHeight, 0xFF000000);
        }
    }
}