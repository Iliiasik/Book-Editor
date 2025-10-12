package bookeditor.client.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public class ModernTextField extends TextFieldWidget {
    private static final int BACKGROUND = 0xFF2D2D30;
    private static final int BORDER = 0xFF3E3E42;
    private static final int BORDER_FOCUSED = 0xFF007ACC;
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int PADDING = 6;

    public ModernTextField(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x + PADDING, y, width - PADDING * 2, height, text);
        this.setDrawsBackground(false);
        this.setEditableColor(TEXT_COLOR);
        this.setUneditableColor(TEXT_COLOR);
    }

    @Override
    public void setX(int x) {
        super.setX(x + PADDING);
    }

    @Override
    public int getX() {
        return super.getX() - PADDING;
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = getX();
        int y = getY();
        int w = this.width + PADDING * 2;
        int h = this.height;

        context.fill(x, y, x + w, y + h, BACKGROUND);

        int borderColor = this.isFocused() ? BORDER_FOCUSED : BORDER;
        int borderWidth = this.isFocused() ? 2 : 1;

        context.fill(x, y, x + w, y + borderWidth, borderColor);
        context.fill(x, y + h - borderWidth, x + w, y + h, borderColor);
        context.fill(x, y, x + borderWidth, y + h, borderColor);
        context.fill(x + w - borderWidth, y, x + w, y + h, borderColor);

        int yOffset = (h - 8) / 2;

        context.getMatrices().push();
        context.getMatrices().translate(0, yOffset, 0);
        context.enableScissor(x + PADDING, y + 2, x + w - PADDING, y + h - 2);
        super.renderButton(context, mouseX, mouseY - yOffset, delta);
        context.disableScissor();
        context.getMatrices().pop();
    }
}