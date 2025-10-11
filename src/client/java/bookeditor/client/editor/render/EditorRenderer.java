package bookeditor.client.editor.render;

import bookeditor.client.editor.brush.BrushTool;
import bookeditor.client.editor.image.ImageInteraction;
import bookeditor.client.editor.mode.EditorMode;
import bookeditor.client.editor.textbox.TextBoxCaret;
import bookeditor.client.editor.textbox.TextBoxInteraction;
import bookeditor.client.editor.textbox.TextBoxRenderer;
import bookeditor.data.BookData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class EditorRenderer {
    private final TextBoxRenderer textBoxRenderer = new TextBoxRenderer();
    private final ImageRenderer imageRenderer = new ImageRenderer();
    private final CaretPainter caretPainter = new CaretPainter();

    public void render(DrawContext ctx, BookData.Page page, EditorMode mode,
                       BrushTool brushTool, ImageInteraction imageInteraction,
                       TextBoxInteraction textBoxInteraction, TextBoxCaret textBoxCaret,
                       TextRenderer textRenderer, boolean isFocused, boolean editable,
                       int startScreenX, int startScreenY, int canvasScreenTop,
                       double scale, int scrollY, int logicalW, int logicalH) {

        brushTool.renderStrokes(ctx, page, startScreenX, startScreenY, scale, scrollY);

        imageInteraction.beginFrame();
        textBoxInteraction.beginFrame();

        if (page != null) {
            for (int i = 0; i < page.nodes.size(); i++) {
                BookData.Node node = page.nodes.get(i);

                if (node instanceof BookData.TextBoxNode box) {
                    renderTextBox(ctx, box, i, mode, textBoxInteraction, textBoxCaret,
                            textRenderer, isFocused, editable, startScreenX, startScreenY, scale, scrollY);
                }
            }
        }

        imageRenderer.render(ctx, page, imageInteraction, startScreenX,
                startScreenY - (int) Math.round(scale * scrollY), canvasScreenTop, scale, logicalW, logicalH);

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

        ctx.fill(boxScreenX, boxScreenY, boxScreenX + boxScreenW, boxScreenY + boxScreenH, 0x0FFFFFFF);

        boolean isSelected = textBoxInteraction.getSelectedTextBoxIndex() == index;
        boolean showCaret = isSelected && mode == EditorMode.TEXT_MODE && textBoxInteraction.isEditingText();
        boolean showSelection = isSelected && mode == EditorMode.TEXT_MODE;

        TextBoxRenderer.CaretPosition caretPos = textBoxRenderer.render(
                ctx, textRenderer, box, textBoxCaret,
                boxScreenX, boxScreenY, scale, showCaret, showSelection
        );

        if (showCaret) {
            caretPainter.renderCaret(ctx, isFocused, editable, false, textBoxCaret.hasSelection(),
                    caretPos.x, caretPos.y, boxScreenX, boxScreenY, scale, textRenderer.fontHeight, caretPos.textSize);
        }

        textBoxInteraction.addTextBoxRect(boxScreenX, boxScreenY, boxScreenW, boxScreenH, index);
    }
}