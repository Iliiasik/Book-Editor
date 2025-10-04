package bookeditor.client.editor.brush;

import bookeditor.data.BookData;
import net.minecraft.client.gui.DrawContext;

public class BrushTool {
    private boolean brushMode = false;
    private int brushColor = 0xFF000000;
    private int brushSize = 3;
    private BookData.Stroke currentStroke = null;

    public boolean isBrushMode() { return brushMode; }
    public void setBrushMode(boolean brushMode) { this.brushMode = brushMode; }
    public void setBrushColor(int argb) { this.brushColor = argb; }
    public void setBrushSize(int px) { this.brushSize = Math.max(1, Math.min(32, px)); }
    public int getBrushSize() { return brushSize; }

    public boolean beginStrokeIfNeeded(boolean editable, BookData.Page page, int mx, int my,
                                       int contentLeft, int contentTop, double scale, int scrollY) {
        if (!editable || !brushMode || page == null) return false;
        currentStroke = new BookData.Stroke();
        currentStroke.color = brushColor;
        currentStroke.thickness = brushSize;
        addStrokePoint(mx, my, contentLeft, contentTop, scale, scrollY);
        return true;
    }

    public boolean continueStrokeIfActive(int mx, int my, int contentLeft, int contentTop, double scale, int scrollY) {
        if (currentStroke == null) return false;
        addStrokePoint(mx, my, contentLeft, contentTop, scale, scrollY);
        return true;
    }

    public boolean endStrokeIfActive(BookData.Page page) {
        if (currentStroke == null || page == null) return false;
        if (!currentStroke.points.isEmpty()) {
            page.strokes.add(currentStroke);
        }
        currentStroke = null;
        return true;
    }

    private void addStrokePoint(int mx, int my, int contentLeft, int contentTop, double scale, int scrollY) {
        if (currentStroke == null) return;
        int lx = (int)Math.floor(((mx - contentLeft) / scale));
        int ly = (int)Math.floor(((my - contentTop) / scale)) + scrollY;
        if (lx < 0) lx = 0; if (ly < 0) ly = 0;
        lx = Math.min(lx, 960);
        ly = Math.min(ly, 600);
        currentStroke.points.add(new BookData.Stroke.Point(lx, ly));
    }

    public void renderStrokes(DrawContext ctx, BookData.Page page, int startScreenX, int startScreenY, double s, int scrollY) {
        if (page == null) return;
        for (BookData.Stroke stroke : page.strokes) {
            int px = Math.max(1, (int)Math.round(s * Math.max(1, stroke.thickness)));
            int half = px / 2;
            for (BookData.Stroke.Point p : stroke.points) {
                int sx = startScreenX + (int)Math.round(s * p.x);
                int sy = startScreenY + (int)Math.round(s * (p.y - scrollY));
                ctx.fill(sx - half, sy - half, sx + half + 1, sy + half + 1, stroke.color);
            }
        }
    }
}