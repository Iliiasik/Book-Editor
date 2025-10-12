package bookeditor.client.gui.screen;

import bookeditor.client.gui.widget.ModernButton;
import bookeditor.client.gui.widget.ModernTextField;
import bookeditor.client.gui.widget.NumericTextField;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ImageInsertScreen extends Screen {

    public interface Callback {
        void onSubmit(String url, int width, int height, boolean isGif);
    }

    private final Screen parent;
    private final Callback callback;
    private ModernTextField urlField;
    private NumericTextField wField;
    private NumericTextField hField;
    private final boolean gif;

    public ImageInsertScreen(Screen parent, Callback callback, boolean gif) {
        super(Text.translatable(gif ? "screen.bookeditor.image_gif" : "screen.bookeditor.image"));
        this.parent = parent;
        this.callback = callback;
        this.gif = gif;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 2 - 60;

        y += 30;

        urlField = new ModernTextField(this.textRenderer, centerX - 150, y, 300, 22, Text.literal(""));
        urlField.setMaxLength(2048);
        addDrawableChild(urlField);

        y += 35;

        wField = new NumericTextField(this.textRenderer, centerX - 150, y, 140, 22, Text.literal(""));
        wField.setText("64");
        addDrawableChild(wField);

        hField = new NumericTextField(this.textRenderer, centerX + 10, y, 140, 22, Text.literal(""));
        hField.setText("64");
        addDrawableChild(hField);

        y += 35;

        addDrawableChild(new ModernButton(centerX - 150, y, 140, 24,
                Text.translatable("gui.cancel"), b -> close()));

        addDrawableChild(new ModernButton(centerX + 10, y, 140, 24,
                Text.translatable("screen.bookeditor.add"), b -> {
            try {
                int w = Integer.parseInt(wField.getText().trim());
                int h = Integer.parseInt(hField.getText().trim());
                String url = urlField.getText().trim();
                if (!url.isEmpty() && callback != null) {
                    callback.onSubmit(url, w, h, gif);
                }
            } catch (NumberFormatException ignored) {
            }
            close();
        }));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx);

        int panelX = this.width / 2 - 160;
        int panelY = this.height / 2 - 80;
        int panelW = 320;
        int panelH = 160;

        ctx.fill(panelX - 2, panelY - 2, panelX + panelW + 2, panelY + panelH + 2, 0xFF1E1E1E);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF2D2D30);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + 2, 0xFF007ACC);

        int titleY = panelY - 28;
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.getTitle(), this.width / 2, titleY, 0xFFFFFFFF);

        int urlLabelY = panelY + 8;
        ctx.drawText(this.textRenderer, Text.translatable("screen.bookeditor.url_label"),
                panelX + 10, urlLabelY, 0xFFE0E0E0, false);

        int sizeLabelY = this.height / 2 - 25;
        ctx.drawText(this.textRenderer, Text.translatable("screen.bookeditor.width_label"),
                panelX + 10, sizeLabelY, 0xFFE0E0E0, false);
        ctx.drawText(this.textRenderer, Text.translatable("screen.bookeditor.height_label"),
                this.width / 2 + 20, sizeLabelY, 0xFFE0E0E0, false);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }
}