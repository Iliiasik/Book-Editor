package bookeditor.client.gui.widget.button;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class ColorPickerDropdown extends ClickableWidget {
    private final java.util.function.Consumer<Integer> onColorChange;
    private int argb;
    private boolean expanded = false;
    private float hoverProgress = 0.0f;
    private long lastFrameTime = System.currentTimeMillis();

    private static final int[] PRESET_COLORS = {
            0xFF000000, 0xFF404040, 0xFF808080, 0xFFC0C0C0, 0xFFFFFFFF,
            0xFFFF0000, 0xFFFF8000, 0xFFFFFF00, 0xFF80FF00, 0xFF00FF00,
            0xFF00FF80, 0xFF00FFFF, 0xFF0080FF, 0xFF0000FF, 0xFF8000FF,
            0xFFFF00FF, 0xFFFF0080, 0xFF800000, 0xFF808000, 0xFF008000,
            0xFF008080, 0xFF000080, 0xFF800080, 0xFF8B4513, 0xFFFFA500
    };

    private static final int COLOR_SIZE = 16;
    private static final int GAP = 2;
    private static final int COLS = 5;

    public ColorPickerDropdown(int x, int y, java.util.function.Consumer<Integer> onColorChange, int initialArgb) {
        super(x, y, 20, 18, Text.literal(""));
        this.onColorChange = onColorChange;
        this.argb = initialArgb;
    }

    public void setArgb(int argb) {
        this.argb = argb;
    }

    public int getArgb() {
        return argb;
    }

    public boolean isExpanded() {
        return expanded;
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

    public void renderDropdown(DrawContext ctx, int mouseX, int mouseY) {
        if (!expanded) return;

        int rows = (PRESET_COLORS.length + COLS - 1) / COLS;
        int dropWidth = COLS * COLOR_SIZE + (COLS + 1) * GAP;
        int dropHeight = rows * COLOR_SIZE + (rows + 1) * GAP;

        int dropX = getX();
        int dropY = getY() + height + 2;

        ctx.getMatrices().push();
        ctx.getMatrices().translate(0, 0, 300);

        ctx.fill(dropX - 1, dropY - 1, dropX + dropWidth + 1, dropY + dropHeight + 1, 0xFF1E1E1E);
        ctx.fill(dropX, dropY, dropX + dropWidth, dropY + dropHeight, 0xFF2D2D30);

        for (int i = 0; i < PRESET_COLORS.length; i++) {
            int col = i % COLS;
            int row = i / COLS;
            int cx = dropX + GAP + col * (COLOR_SIZE + GAP);
            int cy = dropY + GAP + row * (COLOR_SIZE + GAP);

            ctx.fill(cx, cy, cx + COLOR_SIZE, cy + COLOR_SIZE, PRESET_COLORS[i]);

            if (mouseX >= cx && mouseX < cx + COLOR_SIZE && mouseY >= cy && mouseY < cy + COLOR_SIZE) {
                ctx.fill(cx, cy, cx + COLOR_SIZE, cy + 1, 0xFFFFFFFF);
                ctx.fill(cx, cy + COLOR_SIZE - 1, cx + COLOR_SIZE, cy + COLOR_SIZE, 0xFFFFFFFF);
                ctx.fill(cx, cy, cx + 1, cy + COLOR_SIZE, 0xFFFFFFFF);
                ctx.fill(cx + COLOR_SIZE - 1, cy, cx + COLOR_SIZE, cy + COLOR_SIZE, 0xFFFFFFFF);
            }
        }

        ctx.getMatrices().pop();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (expanded) {
            int rows = (PRESET_COLORS.length + COLS - 1) / COLS;
            int dropWidth = COLS * COLOR_SIZE + (COLS + 1) * GAP;
            int dropHeight = rows * COLOR_SIZE + (rows + 1) * GAP;

            int dropX = getX();
            int dropY = getY() + height + 2;

            if (mouseX >= dropX && mouseX < dropX + dropWidth && mouseY >= dropY && mouseY < dropY + dropHeight) {
                for (int i = 0; i < PRESET_COLORS.length; i++) {
                    int col = i % COLS;
                    int row = i / COLS;
                    int cx = dropX + GAP + col * (COLOR_SIZE + GAP);
                    int cy = dropY + GAP + row * (COLOR_SIZE + GAP);

                    if (mouseX >= cx && mouseX < cx + COLOR_SIZE && mouseY >= cy && mouseY < cy + COLOR_SIZE) {
                        this.argb = PRESET_COLORS[i];
                        onColorChange.accept(this.argb);
                        expanded = false;
                        return;
                    }
                }
            }
            expanded = false;
        } else {
            expanded = true;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (expanded) {
            int rows = (PRESET_COLORS.length + COLS - 1) / COLS;
            int dropWidth = COLS * COLOR_SIZE + (COLS + 1) * GAP;
            int dropHeight = rows * COLOR_SIZE + (rows + 1) * GAP;

            int dropX = getX();
            int dropY = getY() + height + 2;

            if (mouseX >= dropX && mouseX < dropX + dropWidth && mouseY >= dropY && mouseY < dropY + dropHeight) {
                onClick(mouseX, mouseY);
                return true;
            }
            expanded = false;
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int addAlpha(int color, float alpha) {
        int a = (int) (255 * alpha);
        return (a << 24) | (color & 0xFFFFFF);
    }

    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        builder.put(net.minecraft.client.gui.screen.narration.NarrationPart.TITLE, Text.translatable("narrator.select.color"));
    }
}