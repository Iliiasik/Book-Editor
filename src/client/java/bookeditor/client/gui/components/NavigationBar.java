package bookeditor.client.gui.components;

import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.widget.IconButton;
import bookeditor.client.gui.widget.ModernButton;
import bookeditor.client.gui.widget.NumericTextField;
import bookeditor.client.util.IconUtils;
import net.minecraft.text.Text;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class NavigationBar {
    private final WidgetHost host;
    private final int x;
    private final int y;
    private final int btnH;
    private final IntSupplier getCurrentPage;
    private final IntSupplier getTotalPages;
    private final IntConsumer setPage;
    private final Runnable previousPage;
    private final Runnable nextPage;

    private ModernButton prevBtn;
    private NumericTextField pageField;
    private IconButton goBtn;
    private ModernButton nextBtn;
    private boolean compactMode = false;

    public NavigationBar(WidgetHost host, int x, int y, int btnH,
                         IntSupplier getCurrentPage, IntSupplier getTotalPages,
                         IntConsumer setPage, Runnable previousPage, Runnable nextPage) {
        this.host = host;
        this.x = x;
        this.y = y;
        this.btnH = btnH;
        this.getCurrentPage = getCurrentPage;
        this.getTotalPages = getTotalPages;
        this.setPage = setPage;
        this.previousPage = previousPage;
        this.nextPage = nextPage;
    }

    public void setCompactMode(boolean compact) {
        this.compactMode = compact;
    }

    public void build() {
        int cx = x;

        prevBtn = new ModernButton(cx, y, 20, btnH, Text.literal("◀"), b -> previousPage.run());
        host.addDrawable(prevBtn);
        cx += 20 + 3;

        pageField = new NumericTextField(host.getTextRenderer(), cx, y, 36, btnH, Text.literal(""));
        pageField.setText(String.valueOf(getCurrentPage.getAsInt() + 1));
        pageField.setMaxLength(4);
        host.addDrawable(pageField);
        cx += 36 + 3;

        if (!compactMode) {
            cx += 35;
        }

        goBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_APPLY,
                Text.translatable("tooltip.bookeditor.go_to_page"), b -> handlePageFieldSubmit());
        host.addDrawable(goBtn);
        cx += 18 + 3;

        nextBtn = new ModernButton(cx, y, 20, btnH, Text.literal("▶"), b -> nextPage.run());
        host.addDrawable(nextBtn);
    }

    public void updateFieldFromState() {
        if (pageField != null) {
            pageField.setText(String.valueOf(getCurrentPage.getAsInt() + 1));
        }
        updateButtonStates();
    }

    public void setVisible(boolean visible) {
        if (prevBtn != null) prevBtn.visible = visible;
        if (pageField != null) pageField.visible = visible;
        if (goBtn != null) goBtn.visible = visible;
        if (nextBtn != null) nextBtn.visible = visible;
    }

    public void updateButtonStates() {
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
        } catch (NumberFormatException ignored) {
        }
        updateFieldFromState();
    }

    public int getPageFieldEndX() {
        if (pageField == null) return x;
        return pageField.getX() + pageField.getWidth();
    }
}