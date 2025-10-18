package bookeditor.client.editor.render;

import bookeditor.client.editor.interaction.ImageInteraction;
import bookeditor.client.editor.mode.EditorMode;
import bookeditor.client.editor.textbox.TextBoxCaret;
import bookeditor.client.editor.interaction.TextBoxInteraction;
import bookeditor.data.BookData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class EditorRenderer {
    private final TextBoxRenderer textBoxRenderer = new TextBoxRenderer();
    private final ImageRenderer imageRenderer = new ImageRenderer();
    private final CaretRenderer caretRenderer = new CaretRenderer();

    public void render(DrawContext ctx, BookData.Page page, EditorMode mode,
                       ImageInteraction imageInteraction,
                       TextBoxInteraction textBoxInteraction, TextBoxCaret textBoxCaret,
                       TextRenderer textRenderer, boolean isFocused, boolean editable,
                       int startScreenX, int startScreenY, int canvasScreenTop,
                       double scale, int scrollY, int logicalW, int logicalH) {

        imageInteraction.beginFrame();
        textBoxInteraction.beginFrame();

        imageRenderer.render(ctx, page, imageInteraction, startScreenX,
                startScreenY - (int) Math.round(scale * scrollY), canvasScreenTop, scale, logicalW, logicalH);

        if (page != null) {
            for (int i = 0; i < page.nodes.size(); i++) {
                BookData.Node node = page.nodes.get(i);

                if (node instanceof BookData.TextBoxNode box) {
                    renderTextBox(ctx, box, i, mode, textBoxInteraction, textBoxCaret,
                            textRenderer, isFocused, editable, startScreenX, startScreenY, scale, scrollY);
                }
            }
        }

        imageInteraction.renderSelectionHandles(ctx);
        textBoxInteraction.renderSelectionHandles(ctx);
    }

    private void renderTextBox(DrawContext ctx, BookData.TextBoxNode box, int index, EditorMode mode,
                               TextBoxInteraction textBoxInteraction, TextBoxCaret textBoxCaret,
                               TextRenderer textRenderer, boolean isFocused, boolean editable,
                               int startScreenX, int startScreenY, double scale, int scrollY) {

        int boxScreenX = startScreenX + (int) Math.round(scale * box.x);
        int boxScreenY = startScreenY + (int) Math.round(scale * (box.y - scrollY));
        int boxScreenW = (int) Math.round(scale * box.width);
        int boxScreenH = (int) Math.round(scale * box.height);

        ctx.fill(boxScreenX, boxScreenY, boxScreenX + boxScreenW, boxScreenY + boxScreenH, box.bgArgb);

        boolean isSelected = textBoxInteraction.getSelectedTextBoxIndex() == index;
        boolean showCaret = isSelected && mode == EditorMode.TEXT_MODE && textBoxInteraction.isEditingText();
        boolean showSelection = isSelected && mode == EditorMode.TEXT_MODE;

        TextBoxRenderer.CaretPosition caretPos = textBoxRenderer.render(
                ctx, textRenderer, box, textBoxCaret,
                boxScreenX, boxScreenY, scale, showCaret, showSelection
        );

        if (showCaret && isFocused) {
            int caretScreenX = boxScreenX + (int) Math.round(scale * caretPos.x);
            int caretScreenY = boxScreenY + (int) Math.round(scale * caretPos.y);
            int caretHeight = (int) Math.round(scale * textRenderer.fontHeight * 1.2f * caretPos.textSize);

            caretRenderer.renderCaret(ctx, caretScreenX, caretScreenY, caretHeight,
                    isFocused, textBoxCaret.hasSelection());
        }

        textBoxInteraction.addTextBoxRect(boxScreenX, boxScreenY, boxScreenW, boxScreenH, index);
    }

    public void resetCaretBlink() {
        caretRenderer.reset();
    }
}