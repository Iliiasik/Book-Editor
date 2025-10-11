package bookeditor.client.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class IconButton extends ButtonWidget {
    private final Identifier iconTexture;
    private boolean selected = false;

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
        super.renderButton(ctx, mouseX, mouseY, delta);

        int iconSize = 16;
        int renderSize = (int) (width * 0.7);
        int iconX = getX() + (width - renderSize) / 2;
        int iconY = getY() + (height - renderSize) / 2;

        ctx.getMatrices().push();
        ctx.getMatrices().translate(iconX, iconY, 0);
        float scale = renderSize / (float) iconSize;
        ctx.getMatrices().scale(scale, scale, 1.0f);
        ctx.drawTexture(iconTexture, 0, 0, 0, 0, iconSize, iconSize, iconSize, iconSize);
        ctx.getMatrices().pop();

        if (selected) {
            int color = 0xFF00BFFF;
            ctx.fill(getX(), getY(), getX() + width, getY() + 1, color);
            ctx.fill(getX(), getY() + height - 1, getX() + width, getY() + height, color);
            ctx.fill(getX(), getY() + 1, getX() + 1, getY() + height - 1, color);
            ctx.fill(getX() + width - 1, getY() + 1, getX() + width, getY() + height - 1, color);
        }
    }
}