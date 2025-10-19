package bookeditor.client.editor.input;

import bookeditor.client.editor.mode.EditorMode;
import bookeditor.client.editor.textbox.StyleParams;
import bookeditor.client.editor.textbox.TextBoxCaret;
import bookeditor.client.editor.textbox.TextBoxEditOps;
import bookeditor.client.editor.interaction.TextBoxInteraction;
import bookeditor.client.gui.widget.editor.EditorState;
import bookeditor.data.BookData;
import org.lwjgl.glfw.GLFW;

import static net.minecraft.client.gui.screen.Screen.hasControlDown;

public class EditorInputHandler {
    private final EditorState state;

    public EditorInputHandler(EditorState state) {
        this.state = state;
    }

    public boolean handleCharTyped(EditorMode mode, BookData.Page page, TextBoxInteraction textBoxInteraction,
                                   TextBoxCaret textBoxCaret, StyleParams style, char chr) {
        if (mode != EditorMode.TEXT_MODE || !textBoxInteraction.isEditingText()) return false;

        int selectedIdx = textBoxInteraction.getSelectedTextBoxIndex();
        if (selectedIdx < 0 || selectedIdx >= page.nodes.size()) return false;

        var node = page.nodes.get(selectedIdx);
        if (!(node instanceof BookData.TextBoxNode box)) return false;

        if (chr == '\r') chr = '\n';
        if (chr < 32 && chr != '\n' && chr != '\t') return false;

        boolean ok = state.textBoxOps.insertChar(box, textBoxCaret, style, chr);
        if (!ok) {
            state.showTransientMessage("Content limit reached", 3000);
            return false;
        }
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
            state.textBoxOps.backspace(box, textBoxCaret);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
            state.textBoxOps.deleteForward(box, textBoxCaret);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            boolean ok = state.textBoxOps.insertChar(box, textBoxCaret, new StyleParams(false, false, false, 0xFF202020, 1.0f), '\n');
            if (!ok) {
                state.showTransientMessage("Content limit reached", 3000);
                return false;
            }
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