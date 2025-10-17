package bookeditor.client.gui.widget;

import bookeditor.client.editor.history.HistoryManager;
import bookeditor.client.editor.mode.EditorMode;
import bookeditor.data.BookData;
import net.minecraft.nbt.NbtCompound;

public class EditorHistoryManager {
    private final HistoryManager history = new HistoryManager();
    private final EditorState state;
    public EditorHistoryManager(EditorState state) {
        this.state = state;
    }
    public void pushSnapshot() {
        if (state.page == null) return;
        history.pushSnapshot(state.page::toNbt);
    }
    public void pushSnapshotOnce() {
        if (state.page != null) history.pushSnapshotOnce(state.page::toNbt);
    }
    public boolean undo() {
        boolean result = history.undo(this::applySnapshot);
        if (result) state.editorRenderer.resetCaretBlink();
        return result;
    }
    public boolean redo() {
        boolean result = history.redo(this::applySnapshot);
        if (result) state.editorRenderer.resetCaretBlink();
        return result;
    }
    public void onContentSet() {
        history.clear();
        pushSnapshot();
        state.editorRenderer.resetCaretBlink();
        if (state.onImageUrlSeen != null && state.page != null) {
            for (BookData.Node n : state.page.nodes) {
                if (n instanceof BookData.ImageNode img && img.url != null && !img.url.isEmpty()) {
                    state.onImageUrlSeen.accept(img.url);
                }
            }
        }
    }
    private void applySnapshot(NbtCompound snap) {
        BookData.Page restored = BookData.Page.fromNbt(snap.copy());
        state.page.nodes.clear();
        state.page.strokes.clear();
        state.page.bgArgb = restored.bgArgb;
        state.page.nodes.addAll(restored.nodes);
        state.page.strokes.addAll(restored.strokes);
        state.imageInteraction.clearSelection();
        state.textBoxInteraction.clearSelection();
        state.textBoxCaret.clearSelection();
        state.textBoxCreationTool.deactivate();
        state.mode = EditorMode.OBJECT_MODE;
        if (state.onImageUrlSeen != null) {
            for (BookData.Node n : state.page.nodes) {
                if (n instanceof BookData.ImageNode img && img.url != null && !img.url.isEmpty()) {
                    state.onImageUrlSeen.accept(img.url);
                }
            }
        }
    }
    public void notifyDirty() {
        history.resetSnapshotArmed();
        if (state.onDirty != null) state.onDirty.run();
    }
}