package bookeditor.client.editor.textbox;

import net.minecraft.client.gui.DrawContext;

public class TextBoxCreationTool {
    private boolean active = false;
    private int previewX = 0;
    private int previewY = 0;
    private int previewWidth = 300;
    private int previewHeight = 100;

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }

    public void updatePreview(int mouseX, int mouseY, int contentLeft, int contentTop, double scale, int scrollY) {
        if (!active) return;

        int localX = (int) Math.floor((mouseX - contentLeft) / scale);
        int localY = (int) Math.floor((mouseY - contentTop) / scale) + scrollY;

        previewX = Math.max(0, Math.min(localX - previewWidth / 2, 960 - previewWidth));
        previewY = Math.max(0, Math.min(localY - previewHeight / 2, 600 - previewHeight));
    }

    public void renderPreview(DrawContext ctx, int contentLeft, int contentTop, double scale, int scrollY) {
        if (!active) return;

        int screenX = contentLeft + (int) Math.round(scale * previewX);
        int screenY = contentTop + (int) Math.round(scale * (previewY - scrollY));
        int screenW = (int) Math.round(scale * previewWidth);
        int screenH = (int) Math.round(scale * previewHeight);

        ctx.fill(screenX, screenY, screenX + screenW, screenY + screenH, 0x4400FF00);

        ctx.fill(screenX, screenY, screenX + screenW, screenY + 2, 0x8800FF00);
        ctx.fill(screenX, screenY + screenH - 2, screenX + screenW, screenY + screenH, 0x8800FF00);
        ctx.fill(screenX, screenY, screenX + 2, screenY + screenH, 0x8800FF00);
        ctx.fill(screenX + screenW - 2, screenY, screenX + screenW, screenY + screenH, 0x8800FF00);
    }

    public int getPreviewX() {
        return previewX;
    }

    public int getPreviewY() {
        return previewY;
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }
}