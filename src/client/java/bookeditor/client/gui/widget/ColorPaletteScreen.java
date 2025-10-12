package bookeditor.client.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ColorPaletteScreen extends Screen {
    private final Screen parent;
    private final java.util.function.Consumer<Integer> onColorSelected;
    private int selectedColor;

    private static final int[] PRESET_COLORS = {
            0xFFFFFFFF, 0xFFC0C0C0, 0xFF808080, 0xFF404040, 0xFF000000,
            0xFFFF0000, 0xFFFF8000, 0xFFFFFF00, 0xFF80FF00, 0xFF00FF00,
            0xFF00FF80, 0xFF00FFFF, 0xFF0080FF, 0xFF0000FF, 0xFF8000FF,
            0xFFFF00FF, 0xFFFF0080, 0xFF800000, 0xFF808000, 0xFF008000,
            0xFF008080, 0xFF000080, 0xFF800080, 0xFF8B4513, 0xFFFFA500,
    };

    public ColorPaletteScreen(Screen parent, java.util.function.Consumer<Integer> onColorSelected, int initialColor) {
        super(Text.translatable("screen.bookeditor.color_palette"));
        this.parent = parent;
        this.onColorSelected = onColorSelected;
        this.selectedColor = initialColor;
    }

    @Override
    protected void init() {
        int startX = this.width / 2 - 125;
        int startY = this.height / 2 - 100;
        int colorSize = 40;
        int gap = 10;

        int cols = 5;
        for (int i = 0; i < PRESET_COLORS.length; i++) {
            int col = i % cols;
            int row = i / cols;
            int x = startX + col * (colorSize + gap);
            int y = startY + row * (colorSize + gap);
            int color = PRESET_COLORS[i];

            addDrawableChild(ButtonWidget.builder(Text.literal(""), b -> {
                selectedColor = color;
                onColorSelected.accept(color);
                close();
            }).dimensions(x, y, colorSize, colorSize).build());
        }

        addDrawableChild(new ModernButton(this.width / 2 - 50, this.height - 40, 100, 20,
                Text.translatable("gui.cancel"), b -> close()));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx);
        super.render(ctx, mouseX, mouseY, delta);

        int startX = this.width / 2 - 125;
        int startY = this.height / 2 - 100;
        int colorSize = 40;
        int gap = 10;
        int cols = 5;

        for (int i = 0; i < PRESET_COLORS.length; i++) {
            int col = i % cols;
            int row = i / cols;
            int x = startX + col * (colorSize + gap);
            int y = startY + row * (colorSize + gap);
            int color = PRESET_COLORS[i];

            ctx.fill(x, y, x + colorSize, y + colorSize, color);
            ctx.fill(x - 1, y - 1, x + colorSize + 1, y, 0xFF000000);
            ctx.fill(x - 1, y + colorSize, x + colorSize + 1, y + colorSize + 1, 0xFF000000);
            ctx.fill(x - 1, y, x, y + colorSize, 0xFF000000);
            ctx.fill(x + colorSize, y, x + colorSize + 1, y + colorSize, 0xFF000000);
        }

        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 130, 0xFFFFFF);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }
}