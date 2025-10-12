package bookeditor.client.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class IconButton extends ButtonWidget {
    private final Identifier iconTexture;
    private boolean selected = false;
    private float hoverProgress = 0.0f;
    private long lastFrameTime = System.currentTimeMillis();

    private static final int BG_COLOR = 0xFF3C3C3C;
    private static final int BG_HOVER = 0xFF505050;
    private static final int BG_SELECTED = 0xFF1E1E1E;
    private static final int BORDER_NORMAL = 0xFF555555;
    private static final int BORDER_SELECTED = 0xFF00BFFF;
    private static final int SHADOW = 0x33000000;

    public IconButton(int x, int y, int width, int height, Identifier iconTexture, Text tooltip, PressAction onPress) {
        super(x, y, width, height, Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER);
        this.iconTexture = iconTexture;
        this.setTooltip(Tooltip.of(tooltip));
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    protected void renderButton(DrawContext ctx, int mouseX, int mouseY, float delta) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastFrameTime) / 1000.0f;
        lastFrameTime = currentTime;

        boolean isHovering = this.isHovered();

        if (isHovering && hoverProgress < 1.0f) {
            hoverProgress = Math.min(1.0f, hoverProgress + deltaTime * 5.0f);
        } else if (!isHovering && hoverProgress > 0.0f) {
            hoverProgress = Math.max(0.0f, hoverProgress - deltaTime * 5.0f);
        }

        int x = getX();
        int y = getY();
        int w = width;
        int h = height;

        ctx.fill(x + 1, y + h, x + w - 1, y + h + 1, 0x33000000);

        int bgColor = selected ? BG_SELECTED : interpolateColor(BG_COLOR, BG_HOVER, hoverProgress);
        ctx.fill(x, y, x + w, y + h, bgColor);

        int borderColor = selected ? BORDER_SELECTED : BORDER_NORMAL;
        if (selected) {
            ctx.fill(x, y, x + w, y + 2, borderColor);
            ctx.fill(x, y + h - 2, x + w, y + h, borderColor);
            ctx.fill(x, y, x + 2, y + h, borderColor);
            ctx.fill(x + w - 2, y, x + w, y + h, borderColor);
        } else {
            int bw = isHovering ? 2 : 1;
            ctx.fill(x, y, x + w, y + bw, borderColor);
            ctx.fill(x, y + h - bw, x + w, y + h, borderColor);
            ctx.fill(x, y, x + bw, y + h, borderColor);
            ctx.fill(x + w - bw, y, x + w, y + h, borderColor);
        }

        int iconSize = 16;
        int renderSize = (int) (width * 0.7);
        int iconX = x + (w - renderSize) / 2;
        int iconY = y + (h - renderSize) / 2;

        ctx.getMatrices().push();
        ctx.getMatrices().translate(iconX, iconY, 0);
        float scale = renderSize / (float) iconSize;
        ctx.getMatrices().scale(scale, scale, 1.0f);

        int tint = active ? 0xFFFFFFFF : 0xFF808080;
        if (isHovering && active) {
            tint = 0xFFFFFFFF;
        }
        ctx.setShaderColor(
                ((tint >> 16) & 0xFF) / 255.0f,
                ((tint >> 8) & 0xFF) / 255.0f,
                (tint & 0xFF) / 255.0f,
                ((tint >> 24) & 0xFF) / 255.0f
        );
        ctx.drawTexture(iconTexture, 0, 0, 0, 0, iconSize, iconSize, iconSize, iconSize);
        ctx.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        ctx.getMatrices().pop();
    }

    private int interpolateColor(int color1, int color2, float t) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}