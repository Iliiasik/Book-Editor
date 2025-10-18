package bookeditor.client.editor.interaction;

import bookeditor.data.BookData;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class TextBoxInteraction {
    public static class Rect {
        public int x, y, w, h, nodeIndex;
    }

    private final List<Rect> textBoxRects = new ArrayList<>();
    private int selectedTextBoxIndex = -1;
    private int resizingHandle = -1;
    private boolean draggingTextBox = false;
    private boolean editingText = false;

    private int dragStartMouseX, dragStartMouseY;
    private int dragStartW, dragStartH;
    private int dragStartX, dragStartY;

    public void beginFrame() {
        textBoxRects.clear();
    }

    public void addTextBoxRect(int x, int y, int w, int h, int nodeIndex) {
        Rect r = new Rect();
        r.x = x;
        r.y = y;
        r.w = w;
        r.h = h;
        r.nodeIndex = nodeIndex;
        textBoxRects.add(r);
    }

    public void clearSelection() {
        selectedTextBoxIndex = -1;
        resizingHandle = -1;
        draggingTextBox = false;
        editingText = false;
    }

    public boolean isEditingText() {
        return editingText;
    }

    public void setEditingText(boolean editing) {
        this.editingText = editing;
    }

    public int getSelectedTextBoxIndex() {
        return selectedTextBoxIndex;
    }

    public boolean mouseClicked(int mx, int my, boolean editable, Runnable pushSnapshotOnce, BookData.Page page) {
        if (page == null) return false;

        for (int i = textBoxRects.size() - 1; i >= 0; i--) {
            Rect r = textBoxRects.get(i);

            if (editable && !editingText) {
                if (inside(mx, my, r.x - 2, r.y - 2, 8, 8)) {
                    select(page, r.nodeIndex, 0, mx, my, pushSnapshotOnce);
                    return true;
                }
                if (inside(mx, my, r.x + r.w - 6, r.y - 2, 8, 8)) {
                    select(page, r.nodeIndex, 1, mx, my, pushSnapshotOnce);
                    return true;
                }
                if (inside(mx, my, r.x - 2, r.y + r.h - 6, 8, 8)) {
                    select(page, r.nodeIndex, 2, mx, my, pushSnapshotOnce);
                    return true;
                }
                if (inside(mx, my, r.x + r.w - 6, r.y + r.h - 6, 8, 8)) {
                    select(page, r.nodeIndex, 3, mx, my, pushSnapshotOnce);
                    return true;
                }
            }

            if (inside(mx, my, r.x, r.y, r.w, r.h)) {
                selectedTextBoxIndex = r.nodeIndex;
                if (editable && !editingText) {
                    pushSnapshotOnce.run();
                    draggingTextBox = true;
                    dragStartMouseX = mx;
                    dragStartMouseY = my;
                    BookData.TextBoxNode box = (BookData.TextBoxNode) page.nodes.get(selectedTextBoxIndex);
                    dragStartX = box.x;
                    dragStartY = box.y;
                    dragStartW = box.width;
                    dragStartH = box.height;
                }
                return true;
            }
        }
        return false;
    }

    public boolean mouseDragged(int mx, int my, boolean editable, double scale, BookData.Page page) {
        if (!editable || page == null || editingText) return false;
        if (selectedTextBoxIndex < 0 || selectedTextBoxIndex >= page.nodes.size()) return false;
        var n = page.nodes.get(selectedTextBoxIndex);
        if (!(n instanceof BookData.TextBoxNode box)) return false;

        int dxl = (int) Math.round((mx - dragStartMouseX) / scale);
        int dyl = (int) Math.round((my - dragStartMouseY) / scale);

        if (resizingHandle >= 0) {
            int newW = dragStartW;
            int newH = dragStartH;
            int newX = dragStartX;
            int newY = dragStartY;

            switch (resizingHandle) {
                case 0 -> {
                    newW = Math.max(50, dragStartW - dxl);
                    newH = Math.max(30, dragStartH - dyl);
                    int right = dragStartX + dragStartW;
                    int bottom = dragStartY + dragStartH;
                    newX = right - newW;
                    newY = bottom - newH;
                    if (newX < 0) {
                        newW += newX;
                        newX = 0;
                    }
                    if (newY < 0) {
                        newH += newY;
                        newY = 0;
                    }
                }
                case 1 -> {
                    newW = Math.max(50, dragStartW + dxl);
                    newH = Math.max(30, dragStartH - dyl);
                    int bottom = dragStartY + dragStartH;
                    newY = bottom - newH;
                    if (newY < 0) {
                        newH += newY;
                        newY = 0;
                    }
                }
                case 2 -> {
                    newW = Math.max(50, dragStartW - dxl);
                    newH = Math.max(30, dragStartH + dyl);
                    int right = dragStartX + dragStartW;
                    newX = right - newW;
                    if (newX < 0) {
                        newW += newX;
                        newX = 0;
                    }
                }
                case 3 -> {
                    newW = Math.max(50, dragStartW + dxl);
                    newH = Math.max(30, dragStartH + dyl);
                }
            }

            if (newX + newW > 960) newW = 960 - newX;
            if (newY + newH > 600) newH = 600 - newY;
            box.width = Math.max(50, newW);
            box.height = Math.max(30, newH);
            if (resizingHandle == 0 || resizingHandle == 2) box.x = Math.max(0, newX);
            if (resizingHandle == 0 || resizingHandle == 1) box.y = Math.max(0, newY);
            return true;
        } else if (draggingTextBox) {
            int newX = dragStartX + dxl;
            int newY = dragStartY + dyl;
            int maxX = Math.max(0, 960 - box.width);
            int maxY = Math.max(0, 600 - box.height);
            box.x = Math.max(0, Math.min(newX, maxX));
            box.y = Math.max(0, Math.min(newY, maxY));
            return true;
        }
        return false;
    }

    public boolean mouseReleased() {
        boolean changed = false;
        if (resizingHandle >= 0) {
            resizingHandle = -1;
            changed = true;
        }
        if (draggingTextBox) {
            draggingTextBox = false;
            changed = true;
        }
        return changed;
    }

    public void renderSelectionHandles(DrawContext ctx) {
        if (selectedTextBoxIndex < 0) return;
        Rect r = null;
        for (int i = textBoxRects.size() - 1; i >= 0; i--) {
            if (textBoxRects.get(i).nodeIndex == selectedTextBoxIndex) {
                r = textBoxRects.get(i);
                break;
            }
        }
        if (r != null) {
            int borderColor = editingText ? 0x8800FF00 : 0x8800A0FF;
            ctx.fill(r.x - 1, r.y - 1, r.x + r.w + 1, r.y, borderColor);
            ctx.fill(r.x - 1, r.y + r.h, r.x + r.w + 1, r.y + r.h + 1, borderColor);
            ctx.fill(r.x - 1, r.y, r.x, r.y + r.h, borderColor);
            ctx.fill(r.x + r.w, r.y, r.x + r.w + 1, r.y + r.h, borderColor);

            if (!editingText) {
                drawHandle(ctx, r.x, r.y);
                drawHandle(ctx, r.x + r.w - 4, r.y);
                drawHandle(ctx, r.x, r.y + r.h - 4);
                drawHandle(ctx, r.x + r.w - 4, r.y + r.h - 4);
            }
        }
    }

    public void deleteSelectedIfTextBox(BookData.Page page) {
        if (page == null) return;
        if (selectedTextBoxIndex >= 0 && selectedTextBoxIndex < page.nodes.size()
                && page.nodes.get(selectedTextBoxIndex) instanceof BookData.TextBoxNode) {
            page.nodes.remove(selectedTextBoxIndex);
            selectedTextBoxIndex = -1;
            editingText = false;
        }
    }

    private void select(BookData.Page page, int nodeIndex, int handle, int mx, int my, Runnable pushSnapshotOnce) {
        selectedTextBoxIndex = nodeIndex;
        resizingHandle = handle;
        dragStartMouseX = mx;
        dragStartMouseY = my;
        BookData.TextBoxNode box = (BookData.TextBoxNode) page.nodes.get(nodeIndex);
        dragStartW = box.width;
        dragStartH = box.height;
        dragStartX = box.x;
        dragStartY = box.y;
        pushSnapshotOnce.run();
    }

    private boolean inside(int x, int y, int rx, int ry, int rw, int rh) {
        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }

    private void drawHandle(DrawContext ctx, int x, int y) {
        ctx.fill(x, y, x + 4, y + 4, 0xFFFFFFFF);
        ctx.fill(x + 1, y + 1, x + 3, y + 3, 0xFF0066CC);
    }
}