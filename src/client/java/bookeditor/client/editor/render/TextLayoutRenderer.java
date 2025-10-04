package bookeditor.client.editor.render;

import bookeditor.client.editor.caret.CaretSelectionModel;
import bookeditor.client.editor.text.TextHitTester;
import bookeditor.data.BookData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class TextLayoutRenderer {

    public static class CaretPos {
        public int x;
        public int y;
    }

    public CaretPos render(DrawContext ctx,
                           TextRenderer textRenderer,
                           BookData.Page page,
                           CaretSelectionModel caret,
                           int startScreenX,
                           int startScreenY,
                           int scrollY,
                           double scale,
                           int textAreaW,
                           int baseLineHeight) {

        CaretPos pos = new CaretPos();
        pos.x = 0; pos.y = 0;

        if (page == null) return pos;

        int selStartNode = -1, selStartOff = 0, selEndNode = -1, selEndOff = 0;
        boolean hasSelection = caret.hasSelection();
        if (hasSelection) {
            int[] st = caret.selectionStart();
            int[] en = caret.selectionEnd();
            selStartNode = st[0]; selStartOff = st[1];
            selEndNode = en[0]; selEndOff = en[1];
        }

        int layoutY = 0;

        for (int i = 0; i < page.nodes.size(); i++) {
            BookData.Node n = page.nodes.get(i);
            if (n instanceof BookData.TextNode tn) {
                String text = tn.text == null ? "" : tn.text;
                float localScale = tn.size;
                Style style = Style.EMPTY.withBold(tn.bold).withItalic(tn.italic).withUnderline(tn.underline).withColor(tn.argb & 0xFFFFFF);

                int idx = 0;
                while (idx <= text.length()) {
                    int lineStart = idx;
                    int lineWidth = 0;
                    boolean forcedBreak = false;
                    while (idx < text.length()) {
                        char ch = text.charAt(idx);
                        if (ch == '\n') { forcedBreak = true; idx++; break; }
                        int cw = (int)Math.ceil(textRenderer.getWidth(String.valueOf(ch)) * localScale);
                        if (lineWidth + cw > textAreaW) break;
                        lineWidth += cw;
                        idx++;
                    }

                    int alignOffset = 0;
                    if (tn.align == BookData.ALIGN_CENTER) alignOffset = Math.max(0, (textAreaW - lineWidth) / 2);
                    else if (tn.align == BookData.ALIGN_RIGHT) alignOffset = Math.max(0, (textAreaW - lineWidth));

                    if (hasSelection && i >= selStartNode && i <= selEndNode) {
                        int hStart = lineStart;
                        int hEnd = idx;
                        if (i == selStartNode) hStart = Math.max(hStart, selStartOff);
                        if (i == selEndNode) hEnd = Math.min(hEnd, selEndOff);
                        if (hStart < hEnd) {
                            int preW = TextHitTester.widthOf(textRenderer, text, lineStart, hStart, localScale);
                            int selW = TextHitTester.widthOf(textRenderer, text, hStart, hEnd, localScale);
                            int sx = startScreenX + (int)Math.round(scale * (alignOffset + preW));
                            int sy = startScreenY + (int)Math.round(scale * (layoutY - scrollY));
                            int sw = Math.max(1, (int)Math.round(scale * selW));
                            int sh = Math.max(1, (int)Math.round(scale * ((baseLineHeight + 2) * localScale)));
                            ctx.fill(sx, sy, sx + sw, sy + sh, 0x5533A0FF);
                        }
                    }

                    int drawX = alignOffset;
                    int ci = lineStart;
                    while (ci < idx) {
                        char ch = text.charAt(ci);
                        if (ch == '\n') { ci++; continue; }
                        if (i == caret.getCaretNode() && ci == caret.getCaretOffset()) {
                            pos.x = drawX;
                            pos.y = layoutY;
                        }
                        int cw = (int)Math.ceil(textRenderer.getWidth(String.valueOf(ch)) * localScale);
                        ctx.getMatrices().push();
                        ctx.getMatrices().translate(startScreenX + (int)Math.round(scale * drawX), startScreenY + (int)Math.round(scale * (layoutY - scrollY)), 0);
                        ctx.getMatrices().scale((float)(scale * localScale), (float)(scale * localScale), 1.0f);
                        MutableText t = Text.literal(String.valueOf(ch)).setStyle(style);
                        ctx.drawText(textRenderer, t, 0, 0, tn.argb & 0xFFFFFF, false);
                        ctx.getMatrices().pop();
                        drawX += cw;
                        ci++;
                    }
                    if (i == caret.getCaretNode() && idx == caret.getCaretOffset()) {
                        pos.x = alignOffset + lineWidth;
                        pos.y = layoutY;
                    }
                    layoutY += (int)((baseLineHeight + 2) * localScale);
                    if (idx >= text.length()) break;
                    if (forcedBreak) {}
                }
            }
        }

        return pos;
    }
}