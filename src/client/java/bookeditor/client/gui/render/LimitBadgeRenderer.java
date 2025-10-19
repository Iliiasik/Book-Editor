package bookeditor.client.gui.render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class LimitBadgeRenderer {
    private LimitBadgeRenderer() {}

    public static void renderLimit(DrawContext ctx,
                                   TextRenderer textRenderer,
                                   int screenWidth,
                                   int toolbarY,
                                   int toolbarBtnHeight,
                                   Object data,
                                   boolean nbtTooLarge) {
        if (data == null) return;
        if (!nbtTooLarge) return;

        int centerX = screenWidth / 2;
        int fontH = textRenderer.fontHeight;

        Text msg = Text.translatable("screen.bookeditor.size_limit_reached");

        int padX = 10;
        int padY = 4;
        int height = padY * 2 + fontH;
        int textW = textRenderer.getWidth(msg);
        int width = textW + padX * 2;
        int bW = Math.min(width, screenWidth - 8);
        int bX = centerX - bW / 2;
        if (bX < 4) bX = 4;
        if (bX + bW + 4 > screenWidth) bX = screenWidth - bW - 4;
        int bY = toolbarY + toolbarBtnHeight + 36;

        ctx.fill(bX, bY, bX + bW, bY + height, 0xEEFF4444);
        ctx.fill(bX - 1, bY - 1, bX + bW + 1, bY, 0xFF662222);
        ctx.fill(bX - 1, bY + height, bX + bW + 1, bY + height + 1, 0xFF662222);
        ctx.fill(bX - 1, bY, bX, bY + height, 0xFF662222);
        ctx.fill(bX + bW, bY, bX + bW + 1, bY + height, 0xFF662222);

        ctx.drawTextWithShadow(textRenderer, msg, bX + padX, bY + padY, 0xFFFFFFFF);
    }
}
