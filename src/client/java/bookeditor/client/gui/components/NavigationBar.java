package bookeditor.client.gui.components;

import bookeditor.client.gui.base.WidgetHost;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class NavigationBar {
    private final WidgetHost host;
    private final int x;
    private final int y;
    private final int height;
    private final IntSupplier currentPageSupplier;
    private final IntSupplier totalPagesSupplier;
    private final Consumer<Integer> gotoPageHandler;
    private final Runnable prevHandler;
    private final Runnable nextHandler;

    private ButtonWidget prevBtn;
    private TextFieldWidget gotoField;
    private ButtonWidget goBtn;
    private ButtonWidget nextBtn;

    private static final int BTN_H = 18;
    private static final int GAP = 5;

    public NavigationBar(WidgetHost host,
                         int x,
                         int y,
                         int height,
                         IntSupplier currentPageSupplier,
                         IntSupplier totalPagesSupplier,
                         Consumer<Integer> gotoPageHandler,
                         Runnable prevHandler,
                         Runnable nextHandler) {
        this.host = host;
        this.x = x;
        this.y = y;
        this.height = Math.max(BTN_H, height);
        this.currentPageSupplier = currentPageSupplier;
        this.totalPagesSupplier = totalPagesSupplier;
        this.gotoPageHandler = gotoPageHandler;
        this.prevHandler = prevHandler;
        this.nextHandler = nextHandler;
    }

    public int build() {
        int navX = x;

        prevBtn = host.addDrawable(ButtonWidget.builder(Text.literal("<"), b -> prevHandler.run())
                .dimensions(navX, y, 18, BTN_H).build());
        navX += 18 + GAP;

        gotoField = new TextFieldWidget(host.getTextRenderer(), navX, y, 36, BTN_H, Text.translatable("screen.bookeditor.goto"));
        gotoField.setText(Integer.toString(currentPageSupplier.getAsInt() + 1));
        host.addDrawable(gotoField);
        navX += 36 + GAP;

        goBtn = host.addDrawable(ButtonWidget.builder(Text.literal("Go"), b -> {
            try {
                int p = Integer.parseInt(gotoField.getText().trim()) - 1;
                gotoPageHandler.accept(p);
            } catch (NumberFormatException ignored) {}
        }).dimensions(navX, y, 28, BTN_H).build());
        navX += 28 + GAP;

        nextBtn = host.addDrawable(ButtonWidget.builder(Text.literal(">"), b -> nextHandler.run())
                .dimensions(navX, y, 18, BTN_H).build());

        int totalWidth = (18 + GAP) + (36 + GAP) + (28 + GAP) + 18;
        return totalWidth;
    }

    public void updateFieldFromState() {
        if (gotoField != null) {
            gotoField.setText(Integer.toString(currentPageSupplier.getAsInt() + 1));
        }
    }

    public void setVisible(boolean v) {
        if (prevBtn != null) prevBtn.visible = v;
        if (gotoField != null) gotoField.visible = v;
        if (goBtn != null) goBtn.visible = v;
        if (nextBtn != null) nextBtn.visible = v;
    }
}