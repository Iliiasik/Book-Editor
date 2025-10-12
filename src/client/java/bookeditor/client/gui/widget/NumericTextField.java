package bookeditor.client.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class NumericTextField extends TextFieldWidget {
    private static final int BACKGROUND = 0xFF2D2D30;
    private static final int BORDER = 0xFF3E3E42;
    private static final int BORDER_FOCUSED = 0xFF007ACC;
    private static final int TEXT_COLOR = 0xFFE0E0E0;

    private Runnable onEnterPressed;

    public NumericTextField(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
        this.setDrawsBackground(false);
        this.setEditableColor(TEXT_COLOR);
        this.setUneditableColor(TEXT_COLOR);
        this.setMaxLength(5);
    }

    public void setOnEnterPressed(Runnable callback) {
        this.onEnterPressed = callback;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (chr >= '0' && chr <= '9') {
            return super.charTyped(chr, modifiers);
        }
        if (chr == '.' && !this.getText().contains(".")) {
            return super.charTyped(chr, modifiers);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (onEnterPressed != null) {
                onEnterPressed.run();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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

        String text = this.getText();
        TextRenderer tr = context.getMatrices().peek() != null ?
                net.minecraft.client.MinecraftClient.getInstance().textRenderer : this.getTextRenderer();

        int textWidth = tr.getWidth(text);
        int textX = x + (w - textWidth) / 2;
        int textY = y + (h - 8) / 2;

        context.enableScissor(x + 2, y + 2, x + w - 2, y + h - 2);

        if (text.isEmpty() && !this.isFocused()) {
            Text message = this.getMessage();
            if (message != null) {
                int msgWidth = tr.getWidth(message);
                int msgX = x + (w - msgWidth) / 2;
                context.drawText(tr, message, msgX, textY, 0xFF808080, false);
            }
        } else {
            context.drawText(tr, text, textX, textY, TEXT_COLOR, false);

            if (this.isFocused() && (System.currentTimeMillis() / 500) % 2 == 0) {
                int cursorX = textX + textWidth;
                context.fill(cursorX, textY - 1, cursorX + 1, textY + 9, TEXT_COLOR);
            }
        }

        context.disableScissor();
    }

    private TextRenderer getTextRenderer() {
        return net.minecraft.client.MinecraftClient.getInstance().textRenderer;
    }
}