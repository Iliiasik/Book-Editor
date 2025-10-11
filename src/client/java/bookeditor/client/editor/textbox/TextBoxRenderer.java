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
        float maxSize = 1.0f;
    }

    private static class CharInfo {
        char ch;
        int width;
        BookData.TextSegment segment;
        int globalCharIndex;
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
        caretPos.textSize = 1.0f;

        if (textBox == null) return caretPos;

        int baseLineHeight = textRenderer.fontHeight;
        int selStart = showSelection && caret.hasSelection() ? caret.selectionStart() : -1;
        int selEnd = showSelection && caret.hasSelection() ? caret.selectionEnd() : -1;

        ctx.enableScissor(screenX, screenY, screenX + (int) (textBox.width * scale), screenY + (int) (textBox.height * scale));

        List<LineInfo> lines = buildLines(textRenderer, textBox);
        int currentY = 0;
        boolean caretFound = false;

        for (int lineIdx = 0; lineIdx < lines.size(); lineIdx++) {
            LineInfo line = lines.get(lineIdx);
            int lineHeight = (int) Math.ceil(baseLineHeight * line.maxSize * 1.2f);

            if (currentY + lineHeight > textBox.height) break;

            int offsetX = calculateAlignOffset(line.align, line.width, textBox.width);
            int currentX = offsetX;

            if (line.chars.isEmpty()) {
                if (showCaret && !caretFound) {
                    int totalCharsBeforeLine = getTotalCharsBeforeLine(lines, lineIdx);
                    if (caret.getCharIndex() == totalCharsBeforeLine) {
                        caretPos.x = offsetX;
                        caretPos.y = currentY;
                        caretPos.textSize = line.maxSize;
                        caretFound = true;
                    }
                }
                currentY += lineHeight;
                continue;
            }

            for (CharInfo charInfo : line.chars) {
                if (showCaret && !caretFound && charInfo.globalCharIndex == caret.getCharIndex()) {
                    caretPos.x = currentX;
                    caretPos.y = currentY;
                    caretPos.textSize = charInfo.segment.size;
                    caretFound = true;
                }

                if (showSelection && charInfo.globalCharIndex >= selStart && charInfo.globalCharIndex < selEnd) {
                    int sx = screenX + (int) (currentX * scale);
                    int sy = screenY + (int) (currentY * scale);
                    int sw = Math.max(1, (int) (charInfo.width * scale));
                    int sh = (int) (lineHeight * scale);
                    ctx.fill(sx, sy, sx + sw, sy + sh, 0x5533A0FF);
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
            }

            if (!line.chars.isEmpty()) {
                CharInfo lastChar = line.chars.get(line.chars.size() - 1);
                if (showCaret && !caretFound && lastChar.globalCharIndex + 1 == caret.getCharIndex()) {
                    caretPos.x = currentX;
                    caretPos.y = currentY;
                    caretPos.textSize = lastChar.segment.size;
                    caretFound = true;
                }
            }

            currentY += lineHeight;
        }

        if (showCaret && !caretFound) {
            int totalChars = getTotalChars(lines);
            if (caret.getCharIndex() >= totalChars) {
                if (lines.isEmpty() || (lines.size() == 1 && lines.get(0).chars.isEmpty())) {
                    caretPos.x = 0;
                    caretPos.y = 0;
                    caretPos.textSize = 1.0f;
                } else {
                    LineInfo lastLine = lines.get(lines.size() - 1);
                    int lastLineY = currentY - (int) Math.ceil(baseLineHeight * lastLine.maxSize * 1.2f);

                    if (lastLine.chars.isEmpty()) {
                        caretPos.x = calculateAlignOffset(lastLine.align, 0, textBox.width);
                        caretPos.y = lastLineY;
                        caretPos.textSize = lastLine.maxSize;
                    } else {
                        CharInfo lastChar = lastLine.chars.get(lastLine.chars.size() - 1);
                        int offsetX = calculateAlignOffset(lastLine.align, lastLine.width, textBox.width);
                        caretPos.x = offsetX + lastLine.width;
                        caretPos.y = lastLineY;
                        caretPos.textSize = lastChar.segment.size;
                    }
                }
            }
        }

        ctx.disableScissor();

        return caretPos;
    }

    private int getTotalChars(List<LineInfo> lines) {
        int total = 0;
        for (LineInfo line : lines) {
            total += line.chars.size();
        }
        return total;
    }

    private int getTotalCharsBeforeLine(List<LineInfo> lines, int lineIndex) {
        int total = 0;
        for (int i = 0; i < lineIndex && i < lines.size(); i++) {
            total += lines.get(i).chars.size();
        }
        return total;
    }

    private List<LineInfo> buildLines(TextRenderer textRenderer, BookData.TextBoxNode textBox) {
        List<LineInfo> lines = new ArrayList<>();

        if (textBox.segments.isEmpty()) {
            LineInfo emptyLine = new LineInfo();
            emptyLine.maxSize = 1.0f;
            emptyLine.align = BookData.ALIGN_LEFT;
            lines.add(emptyLine);
            return lines;
        }

        boolean hasAnyText = false;
        for (BookData.TextSegment seg : textBox.segments) {
            if (!seg.text.isEmpty()) {
                hasAnyText = true;
                break;
            }
        }

        if (!hasAnyText) {
            LineInfo emptyLine = new LineInfo();
            emptyLine.maxSize = textBox.segments.isEmpty() ? 1.0f : textBox.segments.get(0).size;
            emptyLine.align = textBox.segments.isEmpty() ? BookData.ALIGN_LEFT : textBox.segments.get(0).align;
            lines.add(emptyLine);
            return lines;
        }

        LineInfo currentLine = new LineInfo();
        int globalCharIndex = 0;

        for (BookData.TextSegment seg : textBox.segments) {
            currentLine.maxSize = Math.max(currentLine.maxSize, seg.size);

            if (seg.text.isEmpty()) {
                continue;
            }

            for (int i = 0; i < seg.text.length(); i++) {
                char ch = seg.text.charAt(i);

                if (ch == '\n') {
                    currentLine.align = seg.align;
                    lines.add(currentLine);
                    currentLine = new LineInfo();
                    currentLine.maxSize = seg.size;
                    globalCharIndex++;
                    continue;
                }

                int charWidth = (int) Math.ceil(textRenderer.getWidth(String.valueOf(ch)) * seg.size);

                if (currentLine.width + charWidth > textBox.width && !currentLine.chars.isEmpty()) {
                    currentLine.align = seg.align;
                    lines.add(currentLine);
                    currentLine = new LineInfo();
                    currentLine.maxSize = seg.size;
                }

                CharInfo charInfo = new CharInfo();
                charInfo.ch = ch;
                charInfo.width = charWidth;
                charInfo.segment = seg;
                charInfo.globalCharIndex = globalCharIndex;

                currentLine.chars.add(charInfo);
                currentLine.width += charWidth;
                currentLine.maxSize = Math.max(currentLine.maxSize, seg.size);
                globalCharIndex++;
            }
        }

        if (!currentLine.chars.isEmpty()) {
            if (!textBox.segments.isEmpty()) {
                currentLine.align = textBox.segments.get(textBox.segments.size() - 1).align;
            }
            lines.add(currentLine);
        } else if (lines.isEmpty()) {
            LineInfo emptyLine = new LineInfo();
            emptyLine.maxSize = 1.0f;
            emptyLine.align = BookData.ALIGN_LEFT;
            lines.add(emptyLine);
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

        int baseLineHeight = textRenderer.fontHeight;
        List<LineInfo> lines = buildLines(textRenderer, textBox);
        int currentY = 0;

        for (int lineIdx = 0; lineIdx < lines.size(); lineIdx++) {
            LineInfo line = lines.get(lineIdx);
            int lineHeight = (int) Math.ceil(baseLineHeight * line.maxSize * 1.2f);

            if (localY >= currentY && localY < currentY + lineHeight) {
                int offsetX = calculateAlignOffset(line.align, line.width, textBox.width);
                int currentX = offsetX;

                if (line.chars.isEmpty()) {
                    return getTotalCharsBeforeLine(lines, lineIdx);
                }

                for (CharInfo charInfo : line.chars) {
                    if (localX < currentX + charInfo.width / 2) {
                        return charInfo.globalCharIndex;
                    }
                    currentX += charInfo.width;
                }

                return line.chars.get(line.chars.size() - 1).globalCharIndex + 1;
            }

            currentY += lineHeight;
        }

        return getTotalChars(lines);
    }
}