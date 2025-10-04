package bookeditor.client.editor.image;

import bookeditor.data.BookData;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class ImageInteraction {
    public static class Rect { public int x,y,w,h,nodeIndex; }

    private final List<Rect> imageRects = new ArrayList<>();
    private int selectedImageIndex = -1;
    private int resizingHandle = -1;
    private boolean draggingImage = false;

    private int dragStartMouseX, dragStartMouseY;
    private int dragStartW, dragStartH;
    private int dragStartImgX, dragStartImgY;

    public void beginFrame() {
        imageRects.clear();
    }

    public void addImageRect(int x, int y, int w, int h, int nodeIndex) {
        Rect r = new Rect();
        r.x = x; r.y = y; r.w = w; r.h = h; r.nodeIndex = nodeIndex;
        imageRects.add(r);
    }

    public void clearSelection() {
        selectedImageIndex = -1;
        resizingHandle = -1;
        draggingImage = false;
    }

    public boolean mouseClicked(int mx, int my, boolean editable, Runnable pushSnapshotOnce, BookData.Page page) {
        if (page == null) return false;

        for (int i = imageRects.size() - 1; i >= 0; i--) {
            Rect r = imageRects.get(i);
            if (editable && inside(mx, my, r.x - 2, r.y - 2, 8, 8)) { select(page, r.nodeIndex, 0, mx, my, pushSnapshotOnce); return true; }
            if (editable && inside(mx, my, r.x + r.w - 6, r.y - 2, 8, 8)) { select(page, r.nodeIndex, 1, mx, my, pushSnapshotOnce); return true; }
            if (editable && inside(mx, my, r.x - 2, r.y + r.h - 6, 8, 8)) { select(page, r.nodeIndex, 2, mx, my, pushSnapshotOnce); return true; }
            if (editable && inside(mx, my, r.x + r.w - 6, r.y + r.h - 6, 8, 8)) { select(page, r.nodeIndex, 3, mx, my, pushSnapshotOnce); return true; }
            if (inside(mx, my, r.x, r.y, r.w, r.h)) {
                selectedImageIndex = r.nodeIndex;
                if (editable) {
                    pushSnapshotOnce.run();
                    draggingImage = true;
                    dragStartMouseX = mx;
                    dragStartMouseY = my;
                    BookData.ImageNode img = (BookData.ImageNode) page.nodes.get(selectedImageIndex);
                    dragStartImgX = img.x;
                    dragStartImgY = img.y;
                    dragStartW = img.w;
                    dragStartH = img.h;
                }
                return true;
            }
        }
        return false;
    }

    public boolean mouseDragged(int mx, int my, boolean editable, double scale, BookData.Page page) {
        if (!editable || page == null) return false;
        if (selectedImageIndex < 0 || selectedImageIndex >= page.nodes.size()) return false;
        var n = page.nodes.get(selectedImageIndex);
        if (!(n instanceof BookData.ImageNode img)) return false;

        int dxl = (int)Math.round((mx - dragStartMouseX) / scale);
        int dyl = (int)Math.round((my - dragStartMouseY) / scale);

        if (resizingHandle >= 0) {
            int newW = dragStartW;
            int newH = dragStartH;
            int newX = dragStartImgX;
            int newY = dragStartImgY;
            switch (resizingHandle) {
                case 0 -> {
                    newW = Math.max(8, dragStartW - dxl);
                    newH = Math.max(8, dragStartH - dyl);
                    int right = dragStartImgX + dragStartW;
                    int bottom = dragStartImgY + dragStartH;
                    newX = right - newW;
                    newY = bottom - newH;
                    if (newX < 0) { newW += newX; newX = 0; }
                    if (newY < 0) { newH += newY; newY = 0; }
                }
                case 1 -> {
                    newW = Math.max(8, dragStartW + dxl);
                    newH = Math.max(8, dragStartH - dyl);
                    int bottom = dragStartImgY + dragStartH;
                    newY = bottom - newH;
                    if (newY < 0) { newH += newY; newY = 0; }
                }
                case 2 -> {
                    newW = Math.max(8, dragStartW - dxl);
                    newH = Math.max(8, dragStartH + dyl);
                    int right = dragStartImgX + dragStartW;
                    newX = right - newW;
                    if (newX < 0) { newW += newX; newX = 0; }
                }
                case 3 -> {
                    newW = Math.max(8, dragStartW + dxl);
                    newH = Math.max(8, dragStartH + dyl);
                }
            }
            if (newX + newW > 960) newW = 960 - newX;
            if (newY + newH > 600) newH = 600 - newY;
            img.w = Math.max(8, newW);
            img.h = Math.max(8, newH);
            if (resizingHandle == 0 || resizingHandle == 2) img.x = Math.max(0, newX);
            if (resizingHandle == 0 || resizingHandle == 1) img.y = Math.max(0, newY);
            return true;
        } else if (draggingImage) {
            int newX = dragStartImgX + dxl;
            int newY = dragStartImgY + dyl;
            int maxX = Math.max(0, 960 - img.w);
            int maxY = Math.max(0, 600 - img.h);
            img.x = Math.max(0, Math.min(newX, maxX));
            img.y = Math.max(0, Math.min(newY, maxY));
            return true;
        }
        return false;
    }

    public boolean mouseReleased() {
        boolean changed = false;
        if (resizingHandle >= 0) { resizingHandle = -1; changed = true; }
        if (draggingImage) { draggingImage = false; changed = true; }
        return changed;
    }

    public void renderSelectionHandles(DrawContext ctx) {
        if (selectedImageIndex < 0) return;
        Rect r = null;
        for (int i = imageRects.size() - 1; i >= 0; i--) {
            if (imageRects.get(i).nodeIndex == selectedImageIndex) { r = imageRects.get(i); break; }
        }
        if (r != null) {
            ctx.fill(r.x - 1, r.y - 1, r.x + r.w + 1, r.y + r.h + 1, 0x8800A0FF);
            drawHandle(ctx, r.x, r.y);
            drawHandle(ctx, r.x + r.w - 4, r.y);
            drawHandle(ctx, r.x, r.y + r.h - 4);
            drawHandle(ctx, r.x + r.w - 4, r.y + r.h - 4);
        }
    }

    private void select(BookData.Page page, int nodeIndex, int handle, int mx, int my, Runnable pushSnapshotOnce) {
        selectedImageIndex = nodeIndex;
        resizingHandle = handle;
        dragStartMouseX = mx;
        dragStartMouseY = my;
        BookData.ImageNode img = (BookData.ImageNode) page.nodes.get(nodeIndex);
        dragStartW = img.w;
        dragStartH = img.h;
        dragStartImgX = img.x;
        dragStartImgY = img.y;
        pushSnapshotOnce.run();
    }

    private boolean inside(int x, int y, int rx, int ry, int rw, int rh) {
        return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
    }

    private void drawHandle(DrawContext ctx, int x, int y) {
        ctx.fill(x, y, x + 4, y + 4, 0xFFFFFFFF);
        ctx.fill(x + 1, y + 1, x + 3, y + 3, 0xFF000000);
    }

    public int getSelectedImageIndex() {
        return selectedImageIndex;
    }

    public void deleteSelectedIfImage(BookData.Page page) {
        if (page == null) return;
        if (selectedImageIndex >= 0 && selectedImageIndex < page.nodes.size()
                && page.nodes.get(selectedImageIndex) instanceof BookData.ImageNode) {
            page.nodes.remove(selectedImageIndex);
            selectedImageIndex = -1;
        }
    }
}