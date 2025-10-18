package bookeditor.client.gui.components;

import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.widget.button.CustomButton;
import net.minecraft.text.Text;

public class NavigationControls {
    private final CustomButton prevBtn;
    private final CustomButton nextBtn;
    private Runnable onPrev;
    private Runnable onNext;
    private int totalPages;
    private int currentPage;

    public NavigationControls(WidgetHost host, int x, int y, int btnHeight) {
        prevBtn = new CustomButton(x, y, 20, btnHeight, Text.literal("◀"), b -> {
            if (onPrev != null) onPrev.run();
        });
        prevBtn.visible = false;
        host.addDrawable(prevBtn);

        nextBtn = new CustomButton(x + 24, y, 20, btnHeight, Text.literal("▶"), b -> {
            if (onNext != null) onNext.run();
        });
        nextBtn.visible = false;
        host.addDrawable(nextBtn);
    }

    public void setOnPrev(Runnable onPrev) {
        this.onPrev = onPrev;
    }

    public void setOnNext(Runnable onNext) {
        this.onNext = onNext;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = Math.max(0, totalPages);
        updateButtons();
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = Math.max(0, Math.min(currentPage, totalPages - 1));
        updateButtons();
    }

    public void setVisible(boolean visible) {
        prevBtn.visible = visible;
        nextBtn.visible = visible;
        updateButtons();
    }

    private void updateButtons() {
        prevBtn.active = currentPage > 0;
        nextBtn.active = currentPage < totalPages - 1 && totalPages > 1;
    }
}
