package bookeditor.client.gui.screen;

import bookeditor.client.gui.widget.CustomButton;
import bookeditor.client.gui.widget.CustomTextField;
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
    private CustomTextField urlField;
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
        int panelW = 260;
        int panelH = 120;
        int centerX = this.width / 2;
        int panelX = centerX - panelW / 2;
        int panelY = this.height / 2 - panelH / 2;

        int y = panelY + 18;

        urlField = new CustomTextField(this.textRenderer, panelX + 14, y, panelW - 28, 18, Text.literal(""));
        urlField.setMaxLength(2048);
        addDrawableChild(urlField);

        y += 25;

        int labelYOffset = 17;

        wField = new NumericTextField(this.textRenderer, panelX + 14, y + labelYOffset, (panelW - 36) / 2, 18, Text.literal(""));
        wField.setText("64");
        addDrawableChild(wField);

        hField = new NumericTextField(this.textRenderer, panelX + 22 + (panelW - 36) / 2, y + labelYOffset, (panelW - 36) / 2, 18, Text.literal(""));
        hField.setText("64");
        addDrawableChild(hField);

        y += labelYOffset + 18 + 6;

        addDrawableChild(new CustomButton(panelX + 14, y, (panelW - 36) / 2, 20,
                Text.translatable("gui.cancel"), b -> close()));

        addDrawableChild(new CustomButton(panelX + 22 + (panelW - 36) / 2, y, (panelW - 36) / 2, 20,
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

        int panelW = 260;
        int panelH = 120;
        int panelX = this.width / 2 - panelW / 2;
        int panelY = this.height / 2 - panelH / 2;

        ctx.fill(panelX - 2, panelY - 2, panelX + panelW + 2, panelY + panelH + 2, 0xFF1E1E1E);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF2D2D30);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + 2, 0xFF007ACC);

        int titleY = panelY - 10;
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.getTitle(), this.width / 2, titleY, 0xFFFFFFFF);

        int urlLabelY = panelY + 6;
        Text urlLabel = Text.translatable("screen.bookeditor.url_label");
        int urlLabelWidth = this.textRenderer.getWidth(urlLabel);
        ctx.drawText(this.textRenderer, urlLabel, this.width / 2 - urlLabelWidth / 2, urlLabelY, 0xFFE0E0E0, false);

        int sizeLabelY = panelY + 50;

        Text widthLabel = Text.translatable("screen.bookeditor.width_label");
        int widthLabelWidth = this.textRenderer.getWidth(widthLabel);
        int widthLabelX = panelX + 14 + ((panelW - 36) / 2 - widthLabelWidth) / 2;
        ctx.drawText(this.textRenderer, widthLabel, widthLabelX, sizeLabelY, 0xFFE0E0E0, false);

        Text heightLabel = Text.translatable("screen.bookeditor.height_label");
        int heightLabelWidth = this.textRenderer.getWidth(heightLabel);
        int heightLabelX = panelX + 22 + (panelW - 36) / 2 + ((panelW - 36) / 2 - heightLabelWidth) / 2;
        ctx.drawText(this.textRenderer, heightLabel, heightLabelX, sizeLabelY, 0xFFE0E0E0, false);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }
}