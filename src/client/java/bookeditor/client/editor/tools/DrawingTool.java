package bookeditor.client.editor.tools;

import bookeditor.data.BookData;

import java.util.Random;

public enum DrawingTool {
    BRUSH,
    SPRAY,
    LINE,
    RECTANGLE,
    CIRCLE,
    ERASER;

    private static final Random RANDOM = new Random();

    public void applyTool(BookData.Page page, BookData.Stroke stroke, int startX, int startY, int endX, int endY, int size, int color) {
        switch (this) {
            case BRUSH -> {
            }
            case SPRAY -> applySpray(stroke, endX, endY, size);
            case LINE -> applyLine(stroke, startX, startY, endX, endY);
            case RECTANGLE -> applyRectangle(stroke, startX, startY, endX, endY);
            case CIRCLE -> applyCircle(stroke, startX, startY, endX, endY);
        }
    }

    private void applySpray(BookData.Stroke stroke, int x, int y, int size) {
        for (int i = 0; i < 5; i++) {
            int offsetX = RANDOM.nextInt(size * 2) - size;
            int offsetY = RANDOM.nextInt(size * 2) - size;
            stroke.points.add(new BookData.Stroke.Point(x + offsetX, y + offsetY));
        }
    }

    private void applyLine(BookData.Stroke stroke, int x1, int y1, int x2, int y2) {
        stroke.points.clear();
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
        stroke.points.clear();
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
        stroke.points.clear();
        int radius = (int) Math.sqrt(Math.pow(endX - centerX, 2) + Math.pow(endY - centerY, 2));

        for (int angle = 0; angle < 360; angle++) {
            double rad = Math.toRadians(angle);
            int x = centerX + (int) (radius * Math.cos(rad));
            int y = centerY + (int) (radius * Math.sin(rad));
            stroke.points.add(new BookData.Stroke.Point(x, y));
        }
    }
}