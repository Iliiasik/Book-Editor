package bookeditor.client.editor.textbox;

import bookeditor.data.BookData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class TextBoxRenderer {

    public static class CaretPosition {
        public int x;
        public int y;
        public float textSize = 1.0f;
    }

    private static class LineInfo {
        List<CharInfo> chars = new ArrayList<>();
        int width = 0;
        int align = BookData.ALIGN_LEFT;
    }

    private static class CharInfo {
        char ch;
        int width;
        BookData.TextSegment segment;
        int charIndex;
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
        int selStart = showSelection && caret.hasSelection() ? caret.selectionStart() : -1;
        int selEnd = showSelection && caret.hasSelection() ? caret.selectionEnd() : -1;

        ctx.enableScissor(screenX, screenY, screenX + (int) (textBox.width * scale), screenY + (int) (textBox.height * scale));

        List<LineInfo> lines = buildLines(textRenderer, textBox);
        int currentY = 0;
        int charIndex = 0;

        for (LineInfo line : lines) {
            if (currentY + lineHeight > textBox.height) break;

            int offsetX = calculateAlignOffset(line.align, line.width, textBox.width);
            int currentX = offsetX;
            CharInfo lastCharInLine = null;

            for (CharInfo charInfo : line.chars) {
                if (showSelection && charIndex >= selStart && charIndex < selEnd) {
                    int sx = screenX + (int) (currentX * scale);
                    int sy = screenY + (int) (currentY * scale);
                    int sw = Math.max(1, (int) (charInfo.width * scale));
                    int sh = Math.max(1, (int) (lineHeight * scale));
                    ctx.fill(sx, sy, sx + sw, sy + sh, 0x5533A0FF);
                }

                if (showCaret && charIndex == caret.getCharIndex()) {
                    caretPos.x = currentX;
                    caretPos.y = currentY;
                    caretPos.textSize = charInfo.segment.size;
                }

                Style style = Style.EMPTY
                        .withBold(charInfo.segment.bold)
                        .withItalic(charInfo.segment.italic)
                        .withUnderline(charInfo.segment.underline)
                        .withColor(charInfo.segment.argb & 0xFFFFFF);

                ctx.getMatrices().push();
                ctx.getMatrices().translate(
                        screenX + currentX * scale,
                        screenY + currentY * scale,
                        0
                );
                ctx.getMatrices().scale((float) (scale * charInfo.segment.size), (float) (scale * charInfo.segment.size), 1.0f);
                ctx.drawText(textRenderer, Text.literal(String.valueOf(charInfo.ch)).setStyle(style), 0, 0, charInfo.segment.argb & 0xFFFFFF, false);
                ctx.getMatrices().pop();

                currentX += charInfo.width;
                lastCharInLine = charInfo;
                charIndex++;
            }

            if (showCaret && charIndex == caret.getCharIndex()) {
                caretPos.x = currentX;
                caretPos.y = currentY;
                if (lastCharInLine != null) {
                    caretPos.textSize = lastCharInLine.segment.size;
                }
            }

            currentY += lineHeight;
        }

        if (showCaret && charIndex == caret.getCharIndex()) {
            caretPos.x = lines.isEmpty() ? 0 : calculateAlignOffset(BookData.ALIGN_LEFT, 0, textBox.width);
            caretPos.y = currentY;
        }

        ctx.disableScissor();

        return caretPos;
    }

    private List<LineInfo> buildLines(TextRenderer textRenderer, BookData.TextBoxNode textBox) {
        List<LineInfo> lines = new ArrayList<>();
        LineInfo currentLine = new LineInfo();
        int charIndex = 0;

        for (BookData.TextSegment seg : textBox.segments) {
            for (int i = 0; i < seg.text.length(); i++) {
                char ch = seg.text.charAt(i);

                if (ch == '\n') {
                    currentLine.align = seg.align;
                    lines.add(currentLine);
                    currentLine = new LineInfo();
                    charIndex++;
                    continue;
                }

                int charWidth = (int) Math.ceil(textRenderer.getWidth(String.valueOf(ch)) * seg.size);

                if (currentLine.width + charWidth > textBox.width && !currentLine.chars.isEmpty()) {
                    currentLine.align = seg.align;
                    lines.add(currentLine);
                    currentLine = new LineInfo();
                }

                CharInfo charInfo = new CharInfo();
                charInfo.ch = ch;
                charInfo.width = charWidth;
                charInfo.segment = seg;
                charInfo.charIndex = charIndex;

                currentLine.chars.add(charInfo);
                currentLine.width += charWidth;
                charIndex++;
            }
        }

        if (!currentLine.chars.isEmpty()) {
            if (!textBox.segments.isEmpty()) {
                currentLine.align = textBox.segments.get(textBox.segments.size() - 1).align;
            }
            lines.add(currentLine);
        }

        return lines;
    }

    private int calculateAlignOffset(int align, int lineWidth, int boxWidth) {
        switch (align) {
            case BookData.ALIGN_CENTER:
                return Math.max(0, (boxWidth - lineWidth) / 2);
            case BookData.ALIGN_RIGHT:
                return Math.max(0, boxWidth - lineWidth);
            default:
                return 0;
        }
    }

    public int getCharIndexAtPosition(TextRenderer textRenderer, BookData.TextBoxNode textBox, int localX, int localY) {
        if (textBox == null) return 0;

        int lineHeight = (int) Math.ceil(textRenderer.fontHeight * 1.2f);
        List<LineInfo> lines = buildLines(textRenderer, textBox);
        int currentY = 0;
        int charIndex = 0;

        for (LineInfo line : lines) {
            if (localY >= currentY && localY < currentY + lineHeight) {
                int offsetX = calculateAlignOffset(line.align, line.width, textBox.width);
                int currentX = offsetX;

                for (CharInfo charInfo : line.chars) {
                    if (localX >= currentX && localX < currentX + charInfo.width) {
                        if (localX < currentX + charInfo.width / 2) {
                            return charInfo.charIndex;
                        } else {
                            return charInfo.charIndex + 1;
                        }
                    }
                    currentX += charInfo.width;
                }

                return charIndex;
            }

            currentY += lineHeight;
            charIndex += line.chars.size();
        }

        return charIndex;
    }
}