package bookeditor.client.editor.tools;

import bookeditor.data.BookData;

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

        page.strokes.removeIf(stroke -> {
            for (BookData.Stroke.Point p : stroke.points) {
                int dx = p.x - localX;
                int dy = p.y - localY;
                if (Math.sqrt(dx * dx + dy * dy) < eraserSize) {
                    return true;
                }
            }
            return false;
        });
    }
}