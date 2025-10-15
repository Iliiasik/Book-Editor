package bookeditor.client.editor.tools;

import bookeditor.data.BookData;
import net.minecraft.client.gui.DrawContext;

import java.util.Random;

public class AdvancedDrawingTool {
    private static final Random RANDOM = new Random();

    private DrawingTool currentTool = DrawingTool.BRUSH;
    private boolean active = false;
    private int toolColor = 0xFF000000;
    private int toolSize = 3;
    private BookData.Stroke currentStroke = null;

    private int startX = 0;
    private int startY = 0;

    public void setTool(DrawingTool tool) {
        this.currentTool = tool;
        this.active = (tool != null);
    }

    public DrawingTool getCurrentTool() {
        return currentTool;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setColor(int argb) {
        this.toolColor = argb;
    }

    public void setSize(int size) {
        this.toolSize = Math.max(1, Math.min(32, size));
    }

    public boolean beginStroke(BookData.Page page, int mx, int my, int contentLeft, int contentTop, double scale, int scrollY) {
        if (!active || page == null) return false;

        int lx = (int) Math.floor((mx - contentLeft) / scale);
        int ly = (int) Math.floor((my - contentTop) / scale) + scrollY;
        lx = Math.max(0, Math.min(lx, 960));
        ly = Math.max(0, Math.min(ly, 600));

        startX = lx;
        startY = ly;

        currentStroke = new BookData.Stroke();
        currentStroke.color = toolColor;
        currentStroke.thickness = toolSize;

        switch (currentTool) {
            case BRUSH:
                currentStroke.points.add(new BookData.Stroke.Point(lx, ly));
                page.strokes.add(currentStroke);
                break;
            case SPRAY:
                applySpray(currentStroke, lx, ly);
                page.strokes.add(currentStroke);
                break;
            case LINE:
            case RECTANGLE:
            case CIRCLE:
                page.strokes.add(currentStroke);
                break;
        }

        return true;
    }

    public boolean continueStroke(int mx, int my, int contentLeft, int contentTop, double scale, int scrollY) {
        if (currentStroke == null) return false;

        int lx = (int) Math.floor((mx - contentLeft) / scale);
        int ly = (int) Math.floor((my - contentTop) / scale) + scrollY;
        lx = Math.max(0, Math.min(lx, 960));
        ly = Math.max(0, Math.min(ly, 600));

        switch (currentTool) {
            case BRUSH:
                currentStroke.points.add(new BookData.Stroke.Point(lx, ly));
                break;
            case SPRAY:
                applySpray(currentStroke, lx, ly);
                break;
            case LINE:
                currentStroke.points.clear();
                applyLine(currentStroke, startX, startY, lx, ly);
                break;
            case RECTANGLE:
                currentStroke.points.clear();
                applyRectangle(currentStroke, startX, startY, lx, ly);
                break;
            case CIRCLE:
                currentStroke.points.clear();
                applyCircle(currentStroke, startX, startY, lx, ly);
                break;
        }

        return true;
    }

    public boolean endStroke() {
        if (currentStroke == null) return false;
        currentStroke = null;
        return true;
    }

    private void applySpray(BookData.Stroke stroke, int x, int y) {
        for (int i = 0; i < 5; i++) {
            int offsetX = RANDOM.nextInt(toolSize * 2) - toolSize;
            int offsetY = RANDOM.nextInt(toolSize * 2) - toolSize;
            int px = Math.max(0, Math.min(x + offsetX, 960));
            int py = Math.max(0, Math.min(y + offsetY, 600));
            stroke.points.add(new BookData.Stroke.Point(px, py));
        }
    }

    private void applyLine(BookData.Stroke stroke, int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            stroke.points.add(new BookData.Stroke.Point(x1, y1));
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    private void applyRectangle(BookData.Stroke stroke, int x1, int y1, int x2, int y2) {
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);

        for (int x = minX; x <= maxX; x++) {
            stroke.points.add(new BookData.Stroke.Point(x, minY));
            stroke.points.add(new BookData.Stroke.Point(x, maxY));
        }
        for (int y = minY; y <= maxY; y++) {
            stroke.points.add(new BookData.Stroke.Point(minX, y));
            stroke.points.add(new BookData.Stroke.Point(maxX, y));
        }
    }

    private void applyCircle(BookData.Stroke stroke, int centerX, int centerY, int endX, int endY) {
        int radius = (int) Math.sqrt(Math.pow(endX - centerX, 2) + Math.pow(endY - centerY, 2));

        for (int angle = 0; angle < 360; angle++) {
            double rad = Math.toRadians(angle);
            int x = centerX + (int) (radius * Math.cos(rad));
            int y = centerY + (int) (radius * Math.sin(rad));
            x = Math.max(0, Math.min(x, 960));
            y = Math.max(0, Math.min(y, 600));
            stroke.points.add(new BookData.Stroke.Point(x, y));
        }
    }

    public void renderStrokes(DrawContext ctx, BookData.Page page, int startScreenX, int startScreenY, double s, int scrollY) {
        if (page == null) return;
        for (BookData.Stroke stroke : page.strokes) {
            renderStroke(ctx, stroke, startScreenX, startScreenY, s, scrollY);
        }
    }

    private void renderStroke(DrawContext ctx, BookData.Stroke stroke, int startScreenX, int startScreenY, double s, int scrollY) {
        int px = Math.max(1, (int) Math.round(s * Math.max(1, stroke.thickness)));
        int half = px / 2;
        for (BookData.Stroke.Point p : stroke.points) {
            int sx = startScreenX + (int) Math.round(s * p.x);
            int sy = startScreenY + (int) Math.round(s * (p.y - scrollY));
            ctx.fill(sx - half, sy - half, sx + half + 1, sy + half + 1, stroke.color);
        }
    }
}