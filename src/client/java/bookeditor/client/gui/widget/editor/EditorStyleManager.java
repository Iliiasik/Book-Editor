package bookeditor.client.gui.widget.editor;

import bookeditor.client.editor.textbox.StyleParams;
import bookeditor.data.BookData;

public class EditorStyleManager {
    private final EditorState state;
    private final EditorHistoryManager historyManager;

    public EditorStyleManager(EditorState state, EditorHistoryManager historyManager) {
        this.state = state;
        this.historyManager = historyManager;
    }

    public void setBold(boolean bold) {
        state.bold = bold;
    }

    public void setItalic(boolean italic) {
        state.italic = italic;
    }

    public void setUnderline(boolean underline) {
        state.underline = underline;
    }

    public void setColor(int argb) {
        state.argb = argb;
        state.drawingTool.setColor(argb);
    }

    public void setSize(float size) {
        state.size = size;
    }

    public void setTextBoxBgColor(int argb) {
        state.textBoxBgColor = argb;
        if (state.mode == bookeditor.client.editor.mode.EditorMode.OBJECT_MODE && state.textBoxInteraction.getSelectedTextBoxIndex() >= 0) {
            var node = state.page.nodes.get(state.textBoxInteraction.getSelectedTextBoxIndex());
            if (node instanceof BookData.TextBoxNode box) {
                historyManager.pushSnapshotOnce();
                box.bgArgb = argb;
                historyManager.notifyDirty();
            }
        }
    }

    public void setAlignment(int align) {
        if (!state.editable || state.page == null || state.mode != bookeditor.client.editor.mode.EditorMode.TEXT_MODE) return;
        if (state.textBoxInteraction.getSelectedTextBoxIndex() < 0) return;
        var node = state.page.nodes.get(state.textBoxInteraction.getSelectedTextBoxIndex());
        if (!(node instanceof BookData.TextBoxNode box)) return;
        historyManager.pushSnapshotOnce();
        for (BookData.TextSegment seg : box.segments) {
            seg.align = align;
        }
        historyManager.notifyDirty();
    }

    public void applyStyleToSelection() {
        if (!state.editable || state.page == null || state.mode != bookeditor.client.editor.mode.EditorMode.TEXT_MODE) return;
        if (state.textBoxInteraction.getSelectedTextBoxIndex() < 0) return;
        var node = state.page.nodes.get(state.textBoxInteraction.getSelectedTextBoxIndex());
        if (!(node instanceof BookData.TextBoxNode box)) return;
        if (state.textBoxCaret.hasSelection()) {
            historyManager.pushSnapshotOnce();
            state.textBoxOps.applyStyleToSelection(box, state.textBoxCaret, style());
            historyManager.notifyDirty();
        }
    }

    public void copySelection() {
        if (state.textBoxCaret.hasSelection()) {
            int selectedIdx = state.textBoxInteraction.getSelectedTextBoxIndex();
            if (selectedIdx >= 0 && selectedIdx < state.page.nodes.size()) {
                var node = state.page.nodes.get(selectedIdx);
                if (node instanceof BookData.TextBoxNode box) {
                    int selStart = state.textBoxCaret.selectionStart();
                    int selEnd = state.textBoxCaret.selectionEnd();
                    state.clipboard = box.getFullText().substring(selStart, Math.min(selEnd, box.getFullText().length()));
                }
            }
        }
    }

    public void cutSelection() {
        copySelection();
        if (state.textBoxCaret.hasSelection()) {
            historyManager.pushSnapshotOnce();
            int selectedIdx = state.textBoxInteraction.getSelectedTextBoxIndex();
            if (selectedIdx >= 0 && selectedIdx < state.page.nodes.size()) {
                var node = state.page.nodes.get(selectedIdx);
                if (node instanceof BookData.TextBoxNode box) {
                    state.textBoxOps.backspace(box, state.textBoxCaret);
                }
            }
            historyManager.notifyDirty();
        }
    }

    public void paste() {
        if (!state.editable || state.page == null || state.mode != bookeditor.client.editor.mode.EditorMode.TEXT_MODE) return;
        if (state.clipboard.isEmpty()) return;
        int selectedIdx = state.textBoxInteraction.getSelectedTextBoxIndex();
        if (selectedIdx >= 0 && selectedIdx < state.page.nodes.size()) {
            var node = state.page.nodes.get(selectedIdx);
            if (node instanceof BookData.TextBoxNode box) {
                historyManager.pushSnapshotOnce();
                for (char ch : state.clipboard.toCharArray()) {
                    state.textBoxOps.insertChar(box, state.textBoxCaret, style(), ch);
                }
                historyManager.notifyDirty();
            }
        }
    }

    public void selectAll() {
        if (state.mode != bookeditor.client.editor.mode.EditorMode.TEXT_MODE) return;
        int selectedIdx = state.textBoxInteraction.getSelectedTextBoxIndex();
        if (selectedIdx >= 0 && selectedIdx < state.page.nodes.size()) {
            var node = state.page.nodes.get(selectedIdx);
            if (node instanceof BookData.TextBoxNode box) {
                state.textBoxCaret.selectAll(box);
            }
        }
    }

    public void syncStylesFromSelection() {
        if (state.textBoxCaret.hasSelection()) {
            int selectedIdx = state.textBoxInteraction.getSelectedTextBoxIndex();
            if (selectedIdx >= 0 && selectedIdx < state.page.nodes.size()) {
                var node = state.page.nodes.get(selectedIdx);
                if (node instanceof BookData.TextBoxNode box) {
                    int selStart = state.textBoxCaret.selectionStart();
                    if (selStart < box.getFullText().length()) {
                        BookData.TextSegment segment = getSegmentAtPosition(box, selStart);
                        if (segment != null) {
                            state.bold = segment.bold;
                            state.italic = segment.italic;
                            state.underline = segment.underline;
                            state.argb = segment.argb;
                            state.size = segment.size;
                        }
                    }
                }
            }
        }
    }

    public boolean isBold() {
        return state.bold;
    }

    public boolean isItalic() {
        return state.italic;
    }

    public boolean isUnderline() {
        return state.underline;
    }

    public float getSize() {
        return state.size;
    }

    public int getColor() {
        return state.argb;
    }

    public StyleParams getStyle() {
        return style();
    }

    private StyleParams style() {
        return new StyleParams(state.bold, state.italic, state.underline, state.argb, state.size);
    }

    private BookData.TextSegment getSegmentAtPosition(BookData.TextBoxNode box, int pos) {
        int currentPos = 0;
        for (BookData.TextSegment seg : box.segments) {
            if (pos >= currentPos && pos < currentPos + seg.text.length()) {
                return seg;
            }
            currentPos += seg.text.length();
        }
        return box.segments.isEmpty() ? null : box.segments.get(box.segments.size() - 1);
    }
}