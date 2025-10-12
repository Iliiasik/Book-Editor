package bookeditor.client.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class ColorPickerButton extends ClickableWidget implements Drawable, Element, Selectable {
    private final java.util.function.Consumer<Integer> onColorChange;
    private int argb;
    private float hoverProgress = 0.0f;
    private long lastFrameTime = System.currentTimeMillis();

    public ColorPickerButton(int x, int y, java.util.function.Consumer<Integer> onColorChange, int initialArgb) {
        super(x, y, 20, 18, Text.literal(""));
        this.onColorChange = onColorChange;
        this.argb = initialArgb;
    }

    public void setArgb(int argb) {
        this.argb = argb;
    }

    @Override
    protected void renderButton(DrawContext ctx, int mouseX, int mouseY, float delta) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastFrameTime) / 1000.0f;
        lastFrameTime = currentTime;

        boolean isHovering = this.isHovered();

        if (isHovering && hoverProgress < 1.0f) {
            hoverProgress = Math.min(1.0f, hoverProgress + deltaTime * 5.0f);
        } else if (!isHovering && hoverProgress > 0.0f) {
            hoverProgress = Math.max(0.0f, hoverProgress - deltaTime * 5.0f);
        }

        int x = getX();
        int y = getY();
        int w = width;
        int h = height;

        ctx.fill(x + 1, y + h, x + w - 1, y + h + 1, 0x33000000);

        ctx.fill(x + 2, y + 2, x + w - 2, y + h - 2, argb);

        int borderBrightness = (int) (100 + 55 * hoverProgress);
        int borderColor = 0xFF000000 | (borderBrightness << 16) | (borderBrightness << 8) | borderBrightness;

        ctx.fill(x, y, x + w, y + 2, borderColor);
        ctx.fill(x, y + h - 2, x + w, y + h, borderColor);
        ctx.fill(x, y, x + 2, y + h, borderColor);
        ctx.fill(x + w - 2, y, x + w, y + h, borderColor);

        int topGradient = addAlpha(0xFFFFFFFF, 0.2f * (1 + hoverProgress * 0.3f));
        ctx.fill(x + 2, y + 2, x + w - 2, y + h / 2, topGradient);
    }

    private int addAlpha(int color, float alpha) {
        int a = (int) (255 * alpha);
        return (a << 24) | (color & 0xFFFFFF);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.setScreen(new ColorPaletteScreen(client.currentScreen, color -> {
            this.argb = color;
            onColorChange.accept(color);
        }, argb));
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, Text.translatable("narrator.select.color"));
    }
}