package bookeditor.client.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;

public class ColorPickerButton extends PressableWidget {
    private int argb;

    public interface Listener { void onPick(int argb); }
    private final Listener listener;

    public ColorPickerButton(int x, int y, Listener listener, int initialArgb) {
        super(x, y, 20, 20, Text.translatable("screen.bookeditor.color"));
        this.listener = listener;
        this.argb = initialArgb;
    }

    public int getArgb() {
        return argb;
    }

    public void setArgb(int argb) {
        this.argb = argb;
    }

    @Override
    public void onPress() {
        int[] palette = {
                0xFF202020,
                0xFFFFFFFF,
                0xFFFF5555,
                0xFF55FF55,
                0xFF5555FF,
                0xFFFFFF55,
                0xFFFF55FF,
                0xFF55FFFF
        };
        int idx = 0;
        for (int i = 0; i < palette.length; i++) {
            if (palette[i] == argb) { idx = i; break; }
        }
        idx = (idx + 1) % palette.length;
        argb = palette[idx];
        if (listener != null) listener.onPick(argb);
    }

    @Override
    protected void renderButton(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int border = isHovered() ? 0xFFFFFFFF : 0xFFB0B0B0;
        ctx.fill(getX() - 1, getY() - 1, getX() + getWidth() + 1, getY() + getHeight() + 1, border);
        ctx.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), argb);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, this.getNarrationMessage());
        this.appendDefaultNarrations(builder);
    }
}