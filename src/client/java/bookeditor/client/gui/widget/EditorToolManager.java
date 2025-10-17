package bookeditor.client.gui.widget;

import bookeditor.client.editor.mode.EditorMode;
import bookeditor.client.editor.tools.DrawingTool;
import bookeditor.data.BookData;
import org.lwjgl.glfw.GLFW;

public class EditorToolManager {
    private final EditorState state;
    private final EditorHistoryManager historyManager;
    private final EditorStyleManager styleManager;

    public EditorToolManager(EditorState state, EditorHistoryManager historyManager, EditorStyleManager styleManager) {
        this.state = state;
        this.historyManager = historyManager;
        this.styleManager = styleManager;
    }

    public void deactivateAllTools() {
        state.drawingTool.setActive(false);
        state.eraserTool.setActive(false);
        state.textBoxCreationTool.deactivate();
    }

    public void setDrawingToolColor(int argb) {
        state.drawingTool.setColor(argb);
    }

    public void setDrawingTool(DrawingTool tool) {
        deactivateAllTools();
        if (tool == DrawingTool.ERASER) {
            state.eraserTool.setActive(true);
        } else {
            state.drawingTool.setTool(tool);
            state.drawingTool.setActive(true);
        }
        state.mode = EditorMode.OBJECT_MODE;
        state.textBoxInteraction.clearSelection();
    }

    public DrawingTool getCurrentDrawingTool() {
        if (state.eraserTool.isActive()) return DrawingTool.ERASER;
        if (state.drawingTool.isActive()) return state.drawingTool.getCurrentTool();
        return null;
    }

    public void setToolSize(int size) {
        state.drawingTool.setSize(size);
        state.eraserTool.setSize(size);
    }

    public void activateTextBoxTool() {
        deactivateAllTools();
        state.textBoxCreationTool.activate();
        state.mode = EditorMode.OBJECT_MODE;
    }

    public boolean isTextBoxToolActive() {
        return state.textBoxCreationTool.isActive();
    }

    public void insertImage(String url, int w, int h, boolean gif) {
        if (!state.editable || state.page == null) return;
        historyManager.pushSnapshotOnce();
        int maxImgW = Math.max(8, EditorState.LOGICAL_W);
        BookData.ImageNode img = new BookData.ImageNode(url, Math.max(8, Math.min(w, maxImgW)), Math.max(8, h), gif);
        img.absolute = true;
        img.x = 0;
        img.y = Math.max(0, Math.min(state.scrollY + 10, Math.max(0, EditorState.LOGICAL_H - img.h)));
        if (state.onImageUrlSeen != null && url != null && !url.isEmpty()) state.onImageUrlSeen.accept(url);
        state.page.nodes.add(img);
        state.mode = EditorMode.OBJECT_MODE;
        historyManager.notifyDirty();
    }

    public void insertTextBox() {
        if (!state.editable || state.page == null) return;
        historyManager.pushSnapshotOnce();
        BookData.TextBoxNode box = new BookData.TextBoxNode(50, 50 + state.scrollY, 300, 100);
        box.bgArgb = state.textBoxBgColor;
        box.setText("", state.bold, state.italic, state.underline, state.argb, state.size);
        state.page.nodes.add(box);
        state.mode = EditorMode.OBJECT_MODE;
        historyManager.notifyDirty();
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return true;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        RichTextEditorWidget widget = state.getWidget();
        if (!widget.isMouseOver(mouseX, mouseY)) return false;
        widget.setFocused(true);
        int mx = (int) mouseX;
        int my = (int) mouseY;
        if (state.textBoxCreationTool.isActive()) {
            historyManager.pushSnapshotOnce();
            BookData.TextBoxNode box = new BookData.TextBoxNode(
                    state.textBoxCreationTool.getPreviewX(),
                    state.textBoxCreationTool.getPreviewY(),
                    state.textBoxCreationTool.getPreviewWidth(),
                    state.textBoxCreationTool.getPreviewHeight()
            );
            box.bgArgb = state.textBoxBgColor;
            box.setText("", state.bold, state.italic, state.underline, state.argb, state.size);
            state.page.nodes.add(box);
            state.textBoxCreationTool.deactivate();
            historyManager.notifyDirty();
            return true;
        }
        if (state.eraserTool.isActive()) {
            historyManager.pushSnapshotOnce();
            state.eraserTool.erase(state.page, mx, my, state.contentScreenLeft(), state.contentScreenTop(), state.scale(), state.scrollY);
            historyManager.notifyDirty();
            return true;
        }
        if (state.drawingTool.isActive()) {
            historyManager.pushSnapshotOnce();
            state.drawingTool.beginStroke(state.page, mx, my, state.contentScreenLeft(), state.contentScreenTop(), state.scale(), state.scrollY);
            return true;
        }
        EditorMode oldMode = state.mode;
        state.mode = state.mouseHandler.handleMouseClick(mx, my, state.editable, state.page, state.mode,
                state.imageInteraction, state.textBoxInteraction, state.textBoxCaret, state.textBoxRenderer,
                state.textRenderer, state.contentScreenLeft(), state.contentScreenTop(), state.scale(), state.scrollY, historyManager::pushSnapshotOnce);
        if (state.mode == EditorMode.TEXT_MODE || oldMode != state.mode) {
            state.editorRenderer.resetCaretBlink();
        }
        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (state.eraserTool.isActive()) {
            state.eraserTool.erase(state.page, (int) mouseX, (int) mouseY, state.contentScreenLeft(), state.contentScreenTop(), state.scale(), state.scrollY);
            historyManager.notifyDirty();
            return true;
        }
        if (state.drawingTool.isActive()) {
            state.drawingTool.continueStroke((int) mouseX, (int) mouseY, state.contentScreenLeft(), state.contentScreenTop(), state.scale(), state.scrollY);
            return true;
        }
        return state.mouseHandler.handleMouseDrag((int) mouseX, (int) mouseY, state.mode, state.editable,
                state.imageInteraction, state.textBoxInteraction, state.textBoxCaret, state.textBoxRenderer, state.textRenderer,
                state.page, state.contentScreenLeft(), state.contentScreenTop(), state.scale(), state.scrollY);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        RichTextEditorWidget widget = state.getWidget();
        boolean changed = false;
        if (state.imageInteraction.mouseReleased()) changed = true;
        if (state.textBoxInteraction.mouseReleased()) changed = true;
        if (state.drawingTool.endStroke()) changed = true;
        if (changed) historyManager.notifyDirty();
        return widget.superMouseReleased(mouseX, mouseY, button);
    }

    public boolean charTyped(char chr, int modifiers) {
        RichTextEditorWidget widget = state.getWidget();
        if (!state.editable || state.page == null || !widget.isFocused()) return false;
        if (state.drawingTool.isActive() || state.eraserTool.isActive() || state.textBoxCreationTool.isActive()) return false;
        int selectedIdx = state.textBoxInteraction.getSelectedTextBoxIndex();
        if (selectedIdx >= 0 && selectedIdx < state.page.nodes.size()) {
            var node = state.page.nodes.get(selectedIdx);
            if (node instanceof BookData.TextBoxNode box) {
                if (box.getFullText().length() >= EditorState.MAX_TEXTBOX_CHARS) {
                    return false;
                }
            }
        }
        historyManager.pushSnapshotOnce();
        boolean handled = state.inputHandler.handleCharTyped(state.mode, state.page, state.textBoxInteraction, state.textBoxCaret, styleManager.getStyle(), chr);
        if (handled) {
            state.editorRenderer.resetCaretBlink();
            historyManager.notifyDirty();
        }
        return handled;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        RichTextEditorWidget widget = state.getWidget();
        if (!widget.isFocused()) return false;
        if ((keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) && state.page != null) {
            if (state.mode == EditorMode.OBJECT_MODE) {
                if (state.imageInteraction.getSelectedImageIndex() >= 0) {
                    historyManager.pushSnapshotOnce();
                    state.imageInteraction.deleteSelectedIfImage(state.page);
                    historyManager.notifyDirty();
                    return true;
                }
                if (state.textBoxInteraction.getSelectedTextBoxIndex() >= 0) {
                    historyManager.pushSnapshotOnce();
                    state.textBoxInteraction.deleteSelectedIfTextBox(state.page);
                    historyManager.notifyDirty();
                    return true;
                }
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (state.textBoxCreationTool.isActive()) {
                state.textBoxCreationTool.deactivate();
                return true;
            }
            if (state.drawingTool.isActive() || state.eraserTool.isActive()) {
                deactivateAllTools();
                return true;
            }
            if (state.mode == EditorMode.TEXT_MODE) {
                state.mode = EditorMode.OBJECT_MODE;
                state.textBoxInteraction.setEditingText(false);
                state.textBoxCaret.clearSelection();
                return true;
            }
        }
        if (!state.editable || state.page == null || state.drawingTool.isActive() || state.eraserTool.isActive() || state.textBoxCreationTool.isActive()) return false;
        if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT ||
                keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN ||
                keyCode == GLFW.GLFW_KEY_HOME || keyCode == GLFW.GLFW_KEY_END) {
            state.editorRenderer.resetCaretBlink();
        }
        historyManager.pushSnapshotOnce();
        boolean handled = state.inputHandler.handleKeyPressed(state.mode, state.page, state.textBoxInteraction, state.textBoxCaret, keyCode, modifiers);
        if (handled) {
            state.editorRenderer.resetCaretBlink();
            historyManager.notifyDirty();
        }
        return handled;
    }
}