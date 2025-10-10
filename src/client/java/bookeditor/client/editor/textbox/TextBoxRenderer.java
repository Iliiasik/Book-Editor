package bookeditor.client.editor.textbox;

import bookeditor.data.BookData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class TextBoxRenderer {

    public static class CaretPosition {
        public int x;
        public int y;
    }

    public CaretPosition render(DrawContext ctx,
                                TextRenderer textRenderer,
                                BookData.TextBoxNode textBox,
                                TextBoxCaret caret,
                                int screenX,
                                int screenY,
                                double scale,
                                boolean showCaret,
                                boolean showSelection) {

        CaretPosition caretPos = new CaretPosition();
        caretPos.x = 0;
        caretPos.y = 0;

        if (textBox == null) return caretPos;

        int lineHeight = (int) Math.ceil(textRenderer.fontHeight * 1.2f);
        int currentX = 0;
        int currentY = 0;
        int charIndex = 0;

        int selStart = showSelection && caret.hasSelection() ? caret.selectionStart() : -1;
        int selEnd = showSelection && caret.hasSelection() ? caret.selectionEnd() : -1;

        ctx.enableScissor(screenX, screenY, screenX + (int) (textBox.width * scale), screenY + (int) (textBox.height * scale));

        for (BookData.TextSegment seg : textBox.segments) {
            Style style = Style.EMPTY
                    .withBold(seg.bold)
                    .withItalic(seg.italic)
                    .withUnderline(seg.underline)
                    .withColor(seg.argb & 0xFFFFFF);

            for (int i = 0; i < seg.text.length(); i++) {
                char ch = seg.text.charAt(i);

                if (ch == '\n') {
                    if (showCaret && charIndex == caret.getCharIndex()) {
                        caretPos.x = currentX;
                        caretPos.y = currentY;
                    }
                    currentY += lineHeight;
                    currentX = 0;
                    charIndex++;
                    continue;
                }

                int charWidth = (int) Math.ceil(textRenderer.getWidth(String.valueOf(ch)) * seg.size);

                if (currentX + charWidth > textBox.width) {
                    currentY += lineHeight;
                    currentX = 0;
                }

                if (currentY + lineHeight > textBox.height) {
                    break;
                }

                if (showSelection && charIndex >= selStart && charIndex < selEnd) {
                    int sx = screenX + (int) (currentX * scale);
                    int sy = screenY + (int) (currentY * scale);
                    int sw = Math.max(1, (int) (charWidth * scale));
                    int sh = Math.max(1, (int) (lineHeight * scale));
                    ctx.fill(sx, sy, sx + sw, sy + sh, 0x5533A0FF);
                }

                if (showCaret && charIndex == caret.getCharIndex()) {
                    caretPos.x = currentX;
                    caretPos.y = currentY;
                }

                ctx.getMatrices().push();
                ctx.getMatrices().translate(
                        screenX + currentX * scale,
                        screenY + currentY * scale,
                        0
                );
                ctx.getMatrices().scale((float) (scale * seg.size), (float) (scale * seg.size), 1.0f);
                ctx.drawText(textRenderer, Text.literal(String.valueOf(ch)).setStyle(style), 0, 0, seg.argb & 0xFFFFFF, false);
                ctx.getMatrices().pop();

                currentX += charWidth;
                charIndex++;
            }
        }

        if (showCaret && charIndex == caret.getCharIndex()) {
            caretPos.x = currentX;
            caretPos.y = currentY;
        }

        ctx.disableScissor();

        return caretPos;
    }

    public int getCharIndexAtPosition(TextRenderer textRenderer, BookData.TextBoxNode textBox, int localX, int localY) {
        if (textBox == null) return 0;

        int lineHeight = (int) Math.ceil(textRenderer.fontHeight * 1.2f);
        int currentX = 0;
        int currentY = 0;
        int charIndex = 0;

        for (BookData.TextSegment seg : textBox.segments) {
            for (int i = 0; i < seg.text.length(); i++) {
                char ch = seg.text.charAt(i);

                if (ch == '\n') {
                    if (localY >= currentY && localY < currentY + lineHeight) {
                        return charIndex;
                    }
                    currentY += lineHeight;
                    currentX = 0;
                    charIndex++;
                    continue;
                }

                int charWidth = (int) Math.ceil(textRenderer.getWidth(String.valueOf(ch)) * seg.size);

                if (currentX + charWidth > textBox.width) {
                    if (localY >= currentY && localY < currentY + lineHeight) {
                        return charIndex;
                    }
                    currentY += lineHeight;
                    currentX = 0;
                }

                if (currentY + lineHeight > textBox.height) {
                    return charIndex;
                }

                if (localY >= currentY && localY < currentY + lineHeight) {
                    if (localX >= currentX && localX < currentX + charWidth) {
                        if (localX < currentX + charWidth / 2) {
                            return charIndex;
                        } else {
                            return charIndex + 1;
                        }
                    }
                }

                currentX += charWidth;
                charIndex++;
            }
        }

        return charIndex;
    }
}