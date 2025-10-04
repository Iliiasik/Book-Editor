package bookeditor.client.editor.history;

import net.minecraft.nbt.NbtCompound;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class HistoryManager {
    private final Deque<NbtCompound> undoStack = new ArrayDeque<>();
    private final Deque<NbtCompound> redoStack = new ArrayDeque<>();
    private final int maxHistory;

    private boolean snapshotArmed = false;

    public HistoryManager() { this(50); }
    public HistoryManager(int maxHistory) { this.maxHistory = Math.max(1, maxHistory); }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
        snapshotArmed = false;
    }

    public void pushSnapshot(Supplier<NbtCompound> supplier) {
        if (supplier == null) return;
        NbtCompound snap = supplier.get();
        if (snap == null) return;
        if (undoStack.size() >= maxHistory) undoStack.pollFirst();
        undoStack.addLast(snap);
        redoStack.clear();
        snapshotArmed = true;
    }

    public void pushSnapshotNoClear(Supplier<NbtCompound> supplier) {
        if (supplier == null) return;
        NbtCompound snap = supplier.get();
        if (snap == null) return;
        if (undoStack.size() >= maxHistory) undoStack.pollFirst();
        undoStack.addLast(snap);
        snapshotArmed = true;
    }

    public void pushSnapshotOnce(Supplier<NbtCompound> supplier) {
        if (!snapshotArmed) pushSnapshot(supplier);
    }

    public boolean undo(Consumer<NbtCompound> applier) {
        if (undoStack.size() <= 1) return false;
        NbtCompound current = undoStack.pollLast();
        if (current != null) redoStack.addLast(current);
        NbtCompound prev = undoStack.peekLast();
        if (prev != null) {
            if (applier != null) applier.accept(prev.copy());
            trimRedo();
            snapshotArmed = false;
            return true;
        }
        return false;
    }

    public boolean redo(Consumer<NbtCompound> applier) {
        if (redoStack.isEmpty()) return false;
        NbtCompound snap = redoStack.pollLast();
        if (snap != null) {
            if (applier != null) applier.accept(snap.copy());
            pushSnapshotNoClear(() -> snap.copy());
            snapshotArmed = false;
            return true;
        }
        return false;
    }

    private void trimRedo() {
        while (redoStack.size() > maxHistory) redoStack.pollFirst();
    }

    public void resetSnapshotArmed() { snapshotArmed = false; }
}