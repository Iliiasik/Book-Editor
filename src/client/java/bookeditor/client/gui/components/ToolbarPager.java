package bookeditor.client.gui.components;

import bookeditor.client.gui.base.WidgetHost;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ToolbarPager {
    private final int x;
    private final int y;
    private final int gap;
    private final int availableWidth;
    private final NavigationControls navigation;
    private final Consumer<Integer> onPageChanged;

    private final List<ToolbarSection> sections = new ArrayList<>();
    private final List<List<ToolbarWidget>> pages = new ArrayList<>();
    private int currentPage = 0;

    public ToolbarPager(WidgetHost host, int x, int y, int btnH, int gap, int availableWidth, Consumer<Integer> onPageChanged) {
        this.x = x;
        this.y = y;
        this.gap = gap;
        this.availableWidth = availableWidth;
        this.onPageChanged = onPageChanged;
        this.navigation = new NavigationControls(host, x, y, btnH);
        this.navigation.setOnPrev(() -> {
            if (currentPage > 0) showPage(currentPage - 1);
        });
        this.navigation.setOnNext(() -> {
            if (currentPage < pages.size() - 1) showPage(currentPage + 1);
        });
        this.navigation.setVisible(false);
    }

    public void setSections(List<ToolbarSection> sections) {
        this.sections.clear();
        this.sections.addAll(sections);
        buildPages();
    }

    private void buildPages() {
        pages.clear();
        int navButtonsWidth = 48;
        int usableWidth = availableWidth - navButtonsWidth - 20;
        List<ToolbarWidget> current = new ArrayList<>();
        int currentWidth = 0;

        for (ToolbarSection section : sections) {
            int sectionWidth = section.getTotalWidth(gap);
            if (currentWidth > 0 && currentWidth + sectionWidth + 10 > usableWidth) {
                pages.add(current);
                current = new ArrayList<>();
                currentWidth = 0;
            }
            if (currentWidth > 0) currentWidth += 10;
            current.addAll(section.widgets);
            currentWidth += sectionWidth;
        }
        if (!current.isEmpty()) pages.add(current);
        navigation.setTotalPages(pages.size());
        if (currentPage >= pages.size()) currentPage = Math.max(0, pages.size() - 1);
    }

    public void showPage(int pageIndex) {
        if (pages.isEmpty()) return;
        if (pageIndex < 0 || pageIndex >= pages.size()) return;
        currentPage = pageIndex;

        for (ToolbarSection section : sections) {
            for (ToolbarWidget w : section.widgets) w.setVisible(false);
        }

        List<ToolbarWidget> pageWidgets = pages.get(pageIndex);
        int cx = x + 48;
        for (ToolbarWidget w : pageWidgets) {
            w.setPosition(cx, y);
            w.setVisible(true);
            cx += w.getWidth() + gap;
        }

        navigation.setCurrentPage(currentPage);
        if (onPageChanged != null) onPageChanged.accept(currentPage);
    }

    public void setVisible(boolean visible) {
        navigation.setVisible(visible);
        if (!visible) {
            for (ToolbarSection section : sections) {
                for (ToolbarWidget w : section.widgets) w.setVisible(false);
            }
        } else {
            showPage(currentPage);
        }
    }
}
