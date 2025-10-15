package bookeditor.client.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public class CustomTextField extends TextFieldWidget {
    private static final int BACKGROUND = 0xFF2D2D30;
    private static final int BORDER = 0xFF3E3E42;
    private static final int BORDER_FOCUSED = 0xFF007ACC;
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int PADDING = 6;

    public CustomTextField(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
        this.setDrawsBackground(false);
        this.setEditableColor(TEXT_COLOR);
        this.setUneditableColor(TEXT_COLOR);
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = getX();
        int y = getY();
        int w = this.width;
        int h = this.height;

        context.fill(x, y, x + w, y + h, BACKGROUND);

        int borderColor = this.isFocused() ? BORDER_FOCUSED : BORDER;
        int borderWidth = this.isFocused() ? 2 : 1;

        context.fill(x, y, x + w, y + borderWidth, borderColor);
        context.fill(x, y + h - borderWidth, x + w, y + h, borderColor);
        context.fill(x, y, x + borderWidth, y + h, borderColor);
        context.fill(x + w - borderWidth, y, x + w, y + h, borderColor);

        int originalX = super.getX();
        int originalY = super.getY();

        super.setX(x + PADDING);
        super.setY(y + (h - 8) / 2);

        context.enableScissor(x + PADDING, y + 2, x + w - PADDING, y + h - 2);
        super.renderButton(context, mouseX, mouseY, delta);
        context.disableScissor();

        super.setX(originalX);
        super.setY(originalY);
    }
}