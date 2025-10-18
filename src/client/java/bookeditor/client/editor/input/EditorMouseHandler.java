package bookeditor.client.editor.input;

import bookeditor.client.editor.interaction.ImageInteraction;
import bookeditor.client.editor.mode.EditorMode;
import bookeditor.client.editor.textbox.TextBoxCaret;
import bookeditor.client.editor.interaction.TextBoxInteraction;
import bookeditor.client.editor.render.TextBoxRenderer;
import bookeditor.data.BookData;
import net.minecraft.client.font.TextRenderer;

public class EditorMouseHandler {
    private int clickCount = 0;
    private long lastClickTime = 0;

    public EditorMode handleMouseClick(int mx, int my, boolean editable, BookData.Page page,
                                       EditorMode currentMode, ImageInteraction imageInteraction,
                                       TextBoxInteraction textBoxInteraction, TextBoxCaret textBoxCaret,
                                       TextBoxRenderer textBoxRenderer, TextRenderer textRenderer,
                                       int contentScreenLeft, int contentScreenTop, double scale, int scrollY,
                                       Runnable pushSnapshot) {

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < 500) {
            clickCount++;
        } else {
            clickCount = 1;
        }
        lastClickTime = currentTime;

        if (currentMode == EditorMode.TEXT_MODE && textBoxInteraction.isEditingText()) {
            int selectedIdx = textBoxInteraction.getSelectedTextBoxIndex();
            if (selectedIdx >= 0 && selectedIdx < page.nodes.size()) {
                var node = page.nodes.get(selectedIdx);
                if (node instanceof BookData.TextBoxNode box) {
                    int boxScreenX = contentScreenLeft + (int) Math.round(scale * box.x);
                    int boxScreenY = contentScreenTop + (int) Math.round(scale * (box.y - scrollY));
                    int boxScreenW = (int) Math.round(scale * box.width);
                    int boxScreenH = (int) Math.round(scale * box.height);

                    if (mx >= boxScreenX && mx <= boxScreenX + boxScreenW &&
                            my >= boxScreenY && my <= boxScreenY + boxScreenH) {
                        int localX = (int) Math.round((mx - boxScreenX) / scale);
                        int localY = (int) Math.round((my - boxScreenY) / scale);
                        int charIdx = textBoxRenderer.getCharIndexAtPosition(textRenderer, box, localX, localY);
                        textBoxCaret.setCharIndex(charIdx);
                        textBoxCaret.clearSelection();
                        return currentMode;
                    }
                }
            }
            textBoxInteraction.setEditingText(false);
            textBoxCaret.clearSelection();
            currentMode = EditorMode.OBJECT_MODE;
        }

        imageInteraction.clearSelection();
        textBoxInteraction.clearSelection();

        if (imageInteraction.mouseClicked(mx, my, editable, pushSnapshot, page)) {
            return EditorMode.OBJECT_MODE;
        }

        if (textBoxInteraction.mouseClicked(mx, my, editable, pushSnapshot, page)) {
            if (clickCount >= 2 && editable) {
                int selectedIdx = textBoxInteraction.getSelectedTextBoxIndex();
                if (selectedIdx >= 0 && selectedIdx < page.nodes.size()) {
                    var node = page.nodes.get(selectedIdx);
                    if (node instanceof BookData.TextBoxNode box) {
                        textBoxInteraction.setEditingText(true);
                        textBoxCaret.reset();
                        textBoxCaret.ensureWithinBounds(box);
                        return EditorMode.TEXT_MODE;
                    }
                }
            }
            return EditorMode.OBJECT_MODE;
        }

        return currentMode;
    }

    public boolean handleMouseDrag(int mx, int my, EditorMode mode, boolean editable,
                                   ImageInteraction imageInteraction,
                                   TextBoxInteraction textBoxInteraction, TextBoxCaret textBoxCaret,
                                   TextBoxRenderer textBoxRenderer, TextRenderer textRenderer, BookData.Page page,
                                   int contentScreenLeft, int contentScreenTop, double scale, int scrollY) {

        if (!editable) return false;

        if (mode == EditorMode.TEXT_MODE && textBoxInteraction.isEditingText()) {
            int selectedIdx = textBoxInteraction.getSelectedTextBoxIndex();
            if (selectedIdx >= 0 && selectedIdx < page.nodes.size()) {
                var node = page.nodes.get(selectedIdx);
                if (node instanceof BookData.TextBoxNode box) {
                    int boxScreenX = contentScreenLeft + (int) Math.round(scale * box.x);
                    int boxScreenY = contentScreenTop + (int) Math.round(scale * (box.y - scrollY));

                    int localX = (int) Math.round((mx - boxScreenX) / scale);
                    int localY = (int) Math.round((my - boxScreenY) / scale);
                    int charIdx = textBoxRenderer.getCharIndexAtPosition(textRenderer, box, localX, localY);

                    if (!textBoxCaret.hasSelection()) {
                        textBoxCaret.setAnchor(textBoxCaret.getCharIndex());
                    }
                    textBoxCaret.setCharIndex(charIdx);
                    return true;
                }
            }
        }

        if (imageInteraction.mouseDragged(mx, my, true, scale, page)) {
            return true;
        }

        if (textBoxInteraction.mouseDragged(mx, my, true, scale, page)) {
            return true;
        }

        return true;
    }
}