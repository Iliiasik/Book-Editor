package bookeditor.client.editor.render;

import bookeditor.client.editor.interaction.ImageInteraction;
import bookeditor.client.util.ImageCache;
import bookeditor.data.BookData;
import net.minecraft.client.gui.DrawContext;

public class ImageRenderer {

    public void render(DrawContext ctx,
                       BookData.Page page,
                       ImageInteraction imageInteraction,
                       int startScreenX,
                       int startScreenY,
                       int canvasScreenTop,
                       double scale,
                       int logicalW,
                       int logicalH) {
        if (page == null) return;

        for (int i = 0; i < page.nodes.size(); i++) {
            BookData.Node n = page.nodes.get(i);
            if (n instanceof BookData.ImageNode img) {
                int drawW = Math.max(8, img.w);
                int drawH = Math.max(8, img.h);
                int imgX = startScreenX + (int)Math.round(scale * img.x);
                int imgY = startScreenY + (int)Math.round(scale * img.y);

                var tex = ImageCache.getTexture(img.url);
                boolean texReady = tex != null && net.minecraft.client.MinecraftClient.getInstance().getTextureManager().getTexture(tex) != null;
                int sw = Math.max(1, (int)Math.round(scale * drawW));
                int sh = Math.max(1, (int)Math.round(scale * drawH));
                if (imgY < (canvasScreenTop + (int)Math.floor(scale * (logicalH + 8*2))) && imgY + sh > canvasScreenTop) {
                    if (texReady) ctx.drawTexture(tex, imgX, imgY, 0, 0, sw, sh, sw, sh);
                    else { ctx.fill(imgX, imgY, imgX + sw, imgY + sh, 0xFFCCCCCC); }
                }
                imageInteraction.addImageRect(imgX, imgY, sw, sh, i);
            }
        }
    }
}