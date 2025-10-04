package bookeditor.client.gui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class ImageInsertScreen extends Screen {

    public interface Callback {
        void onSubmit(String url, int width, int height, boolean isGif);
    }

    private final Screen parent;
    private final Callback callback;
    private TextFieldWidget urlField;
    private TextFieldWidget wField;
    private TextFieldWidget hField;
    private final boolean gif;

    public ImageInsertScreen(Screen parent, Callback callback, boolean gif) {
        super(Text.translatable(gif ? "screen.bookeditor.image_gif" : "screen.bookeditor.image"));
        this.parent = parent;
        this.callback = callback;
        this.gif = gif;
    }

    @Override
    protected void init() {
        int centerX = this.width/2;
        int y = this.height/2 - 40;

        urlField = new TextFieldWidget(this.textRenderer, centerX - 120, y, 240, 20, Text.literal("URL"));
        urlField.setMaxLength(2048);
        urlField.setPlaceholder(Text.translatable("screen.bookeditor.url_placeholder"));
        addDrawableChild(urlField);

        wField = new TextFieldWidget(this.textRenderer, centerX - 120, y + 26, 110, 20, Text.literal("W"));
        wField.setText("64");
        addDrawableChild(wField);

        hField = new TextFieldWidget(this.textRenderer, centerX + 10, y + 26, 110, 20, Text.literal("H"));
        hField.setText("64");
        addDrawableChild(hField);

        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.bookeditor.cancel"), b -> close())
                .dimensions(centerX - 120, y + 60, 110, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.bookeditor.add"), b -> {
            try {
                int w = Integer.parseInt(wField.getText().trim());
                int h = Integer.parseInt(hField.getText().trim());
                if (callback != null) callback.onSubmit(urlField.getText().trim(), w, h, gif);
            } catch (NumberFormatException ignored) {}
            close();
        }).dimensions(centerX + 10, y + 60, 110, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx);
        super.render(ctx, mouseX, mouseY, delta);
        int centerX = this.width/2;
        int y = this.height/2 - 60;
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.getTitle(), centerX, y, 0xFFFFFF);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }
}