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
        int borderColor = isHovered() ? 0xFF666666 : 0xFF000000;

        ctx.fill(getX() - 1, getY() - 1, getX() + width + 1, getY() + height + 1, borderColor);
        ctx.fill(getX(), getY(), getX() + width, getY() + height, argb);
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