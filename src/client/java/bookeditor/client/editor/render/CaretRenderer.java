package bookeditor.client.editor.render;

import net.minecraft.client.gui.DrawContext;

public class CaretRenderer {
    private long lastBlinkTime = 0;
    private boolean visible = true;
    private static final long BLINK_INTERVAL = 530;

    public void reset() {
        lastBlinkTime = System.currentTimeMillis();
        visible = true;
    }

    public void renderCaret(DrawContext ctx, int screenX, int screenY, int height, boolean isFocused, boolean hasSelection) {
        if (!isFocused || hasSelection) return;

        long now = System.currentTimeMillis();
        if (now - lastBlinkTime > BLINK_INTERVAL) {
            visible = !visible;
            lastBlinkTime = now;
        }

        if (visible) {
            ctx.fill(screenX, screenY, screenX + 1, screenY + height, 0xFF000000);
        }
    }
}