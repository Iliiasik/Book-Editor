package bookeditor.client.gui.components;

import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.widget.CustomButton;
import bookeditor.client.gui.widget.IconButton;
import bookeditor.client.gui.widget.NumericTextField;
import bookeditor.client.util.IconUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class BookNavigator {
    private final WidgetHost host;
    private final int screenWidth;
    private final int y;
    private final int btnH;
    private final IntSupplier getCurrentPage;
    private final IntSupplier getTotalPages;
    private final IntConsumer setPage;

    private CustomButton prevBtn;
    private NumericTextField pageField;
    private IconButton goBtn;
    private CustomButton nextBtn;
    private int totalPagesTextWidth = 0;

    public BookNavigator(WidgetHost host, int screenWidth, int y, int btnH,
                         IntSupplier getCurrentPage, IntSupplier getTotalPages,
                         IntConsumer setPage) {
        this.host = host;
        this.screenWidth = screenWidth;
        this.y = y;
        this.btnH = btnH;
        this.getCurrentPage = getCurrentPage;
        this.getTotalPages = getTotalPages;
        this.setPage = setPage;
    }

    public void build() {
        int margin = 10;
        String maxPages = "/ 999";
        totalPagesTextWidth = host.getTextRenderer().getWidth(maxPages);

        int totalWidth = 20 + 3 + 36 + totalPagesTextWidth + 5 + 18 + 3 + 20;
        int startX = screenWidth - margin - totalWidth;
        int cx = startX;

        prevBtn = new CustomButton(cx, y, 20, btnH, Text.literal("◀"), b -> {
            int current = getCurrentPage.getAsInt();
            if (current > 0) setPage.accept(current - 1);
        });
        host.addDrawable(prevBtn);
        cx += 20 + 3;

        pageField = new NumericTextField(host.getTextRenderer(), cx, y, 36, btnH, Text.literal(""));
        pageField.setText(String.valueOf(getCurrentPage.getAsInt() + 1));
        pageField.setMaxLength(4);
        pageField.setOnEnterPressed(this::handlePageFieldSubmit);
        host.addDrawable(pageField);
        cx += 36 + totalPagesTextWidth + 5;

        goBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_APPLY,
                Text.translatable("tooltip.bookeditor.go_to_page"), b -> handlePageFieldSubmit());
        host.addDrawable(goBtn);
        cx += 18 + 3;

        nextBtn = new CustomButton(cx, y, 20, btnH, Text.literal("▶"), b -> {
            int current = getCurrentPage.getAsInt();
            if (current < getTotalPages.getAsInt() - 1) setPage.accept(current + 1);
        });
        host.addDrawable(nextBtn);
    }

    public void updateState() {
        if (pageField != null) {
            pageField.setText(String.valueOf(getCurrentPage.getAsInt() + 1));
        }
        if (prevBtn != null) {
            prevBtn.active = getCurrentPage.getAsInt() > 0;
        }
        if (nextBtn != null) {
            nextBtn.active = getCurrentPage.getAsInt() < getTotalPages.getAsInt() - 1;
        }
    }

    public void handlePageFieldSubmit() {
        if (pageField == null) return;
        try {
            int page = Integer.parseInt(pageField.getText()) - 1;
            if (page >= 0 && page < getTotalPages.getAsInt()) {
                setPage.accept(page);
            }
        } catch (NumberFormatException ignored) {}
        updateState();
    }

    public void renderPageCounter(DrawContext ctx, TextRenderer textRenderer, int totalPages) {
        if (pageField == null) return;
        String counter = "/ " + totalPages;
        int counterX = pageField.getX() + pageField.getWidth() + 5;
        int counterY = pageField.getY() + (pageField.getHeight() - 8) / 2;
        ctx.drawText(textRenderer, Text.literal(counter), counterX, counterY, 0xFFE0E0E0, false);
    }
}