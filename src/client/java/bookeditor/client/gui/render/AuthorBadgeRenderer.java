package bookeditor.client.gui.render;

import bookeditor.data.BookData;
import bookeditor.util.SkullStackUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class AuthorBadgeRenderer {
    private AuthorBadgeRenderer() {}

    public static void renderBadge(DrawContext ctx,
                                   TextRenderer textRenderer,
                                   int screenWidth,
                                   int toolbarY,
                                   int toolbarBtnHeight,
                                   BookData data) {
        if (data == null || (!data.signed)) return;

        int centerX = screenWidth / 2;
        int headSize = 20;
        int gap = 6;
        int padX = 8;
        int padY = 3;
        int y = toolbarY + (toolbarBtnHeight - headSize) / 2;

        int nameW = data.authorName == null ? 0 : textRenderer.getWidth(data.authorName);
        int badgeW = padX * 2 + nameW;
        int groupW = (data.authorName == null || data.authorName.isEmpty()) ? headSize : headSize + gap + badgeW;

        int headX = centerX - groupW / 2;

        if (data.title != null && !data.title.isEmpty()) {
            int titleW = textRenderer.getWidth(data.title);
            int titleX = centerX - titleW / 2;
            int titleY = Math.max(0, toolbarY - textRenderer.fontHeight - 4);
            ctx.drawTextWithShadow(textRenderer, Text.literal(data.title), titleX, titleY, 0xFFFFFFFF);
        }

        if (data.authorName != null && !data.authorName.isEmpty()) {
            int badgeX = headX + headSize + gap;
            int badgeY = y + (headSize - (padY * 2 + textRenderer.fontHeight)) / 2;
            int badgeH = padY * 2 + textRenderer.fontHeight;

            int badgeBg = 0x553388FF;
            int badgeBorder = 0x883388FF;
            ctx.fill(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH, badgeBg);
            ctx.fill(badgeX - 1, badgeY - 1, badgeX + badgeW + 1, badgeY, badgeBorder);
            ctx.fill(badgeX - 1, badgeY + badgeH, badgeX + badgeW + 1, badgeY + badgeH + 1, badgeBorder);
            ctx.fill(badgeX - 1, badgeY, badgeX, badgeY + badgeH, badgeBorder);
            ctx.fill(badgeX + badgeW, badgeY, badgeX + badgeW + 1, badgeY + badgeH, badgeBorder);

            ctx.drawTextWithShadow(textRenderer, Text.literal(data.authorName), badgeX + padX, badgeY + padY, 0xFFFFFFFF);
        }

        if (data.authorUuid != null || (data.authorName != null && !data.authorName.isEmpty())) {
            var skull = SkullStackUtil.playerHeadStack(data.authorName, data.authorUuid);
            float scale = headSize / 16f;
            ctx.getMatrices().push();
            ctx.getMatrices().translate(headX, y, 0);
            ctx.getMatrices().scale(scale, scale, 1f);
            ctx.drawItem(skull, 0, 0);
            ctx.getMatrices().pop();
        }
    }
}