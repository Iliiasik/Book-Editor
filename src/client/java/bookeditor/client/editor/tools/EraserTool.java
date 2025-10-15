package bookeditor.client.editor.tools;

import bookeditor.data.BookData;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;

public class EraserTool {
    private boolean active = false;
    private int eraserSize = 10;

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setSize(int size) {
        this.eraserSize = Math.max(5, Math.min(50, size));
    }

    public int getSize() {
        return eraserSize;
    }

    public void erase(BookData.Page page, int mx, int my, int contentLeft, int contentTop, double scale, int scrollY) {
        if (!active || page == null) return;

        int localX = (int) Math.floor((mx - contentLeft) / scale);
        int localY = (int) Math.floor((my - contentTop) / scale) + scrollY;

        for (BookData.Stroke stroke : new ArrayList<>(page.strokes)) {
            stroke.points.removeIf(p -> {
                int dx = p.x - localX;
                int dy = p.y - localY;
                return Math.sqrt(dx * dx + dy * dy) < eraserSize;
            });

            if (stroke.points.isEmpty()) {
                page.strokes.remove(stroke);
            }
        }
    }

    public void renderPreview(DrawContext ctx, int mx, int my, int contentLeft, int contentTop, double scale, int scrollY) {
    }
}