package bookeditor.client.editor.input;

import bookeditor.client.editor.mode.EditorMode;
import bookeditor.client.editor.text.StyleParams;
import bookeditor.client.editor.textbox.TextBoxCaret;
import bookeditor.client.editor.textbox.TextBoxEditOps;
import bookeditor.client.editor.textbox.TextBoxInteraction;
import bookeditor.data.BookData;
import org.lwjgl.glfw.GLFW;

import static net.minecraft.client.gui.screen.Screen.hasControlDown;

public class EditorInputHandler {
    private final TextBoxEditOps textBoxOps = new TextBoxEditOps();

    public boolean handleCharTyped(EditorMode mode, BookData.Page page, TextBoxInteraction textBoxInteraction,
                                   TextBoxCaret textBoxCaret, StyleParams style, char chr) {
        if (mode != EditorMode.TEXT_MODE || !textBoxInteraction.isEditingText()) return false;

        int selectedIdx = textBoxInteraction.getSelectedTextBoxIndex();
        if (selectedIdx < 0 || selectedIdx >= page.nodes.size()) return false;

        var node = page.nodes.get(selectedIdx);
        if (!(node instanceof BookData.TextBoxNode box)) return false;

        if (chr == '\r') chr = '\n';
        if (chr < 32 && chr != '\n' && chr != '\t') return false;

        textBoxOps.insertChar(box, textBoxCaret, style, chr);
        return true;
    }

    public boolean handleKeyPressed(EditorMode mode, BookData.Page page, TextBoxInteraction textBoxInteraction,
                                    TextBoxCaret textBoxCaret, int keyCode, int modifiers) {
        if (mode != EditorMode.TEXT_MODE || !textBoxInteraction.isEditingText()) return false;

        int selectedIdx = textBoxInteraction.getSelectedTextBoxIndex();
        if (selectedIdx < 0 || selectedIdx >= page.nodes.size()) return false;

        var node = page.nodes.get(selectedIdx);
        if (!(node instanceof BookData.TextBoxNode box)) return false;

        boolean ctrl = hasControlDown();

        if (ctrl && keyCode == GLFW.GLFW_KEY_A) {
            textBoxCaret.selectAll(box);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            textBoxOps.backspace(box, textBoxCaret);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
            textBoxOps.deleteForward(box, textBoxCaret);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            textBoxOps.insertChar(box, textBoxCaret, new StyleParams(false, false, false, 0xFF202020, 1.0f), '\n');
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
                if (!textBoxCaret.hasSelection()) {
                    textBoxCaret.setAnchor(textBoxCaret.getCharIndex());
                }
                textBoxCaret.moveLeft(box);
            } else {
                textBoxCaret.moveLeft(box);
                textBoxCaret.clearSelection();
            }
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
                if (!textBoxCaret.hasSelection()) {
                    textBoxCaret.setAnchor(textBoxCaret.getCharIndex());
                }
                textBoxCaret.moveRight(box);
            } else {
                textBoxCaret.moveRight(box);
                textBoxCaret.clearSelection();
            }
            return true;
        }

        return false;
    }
}