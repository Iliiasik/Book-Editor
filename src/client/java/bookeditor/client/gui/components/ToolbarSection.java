package bookeditor.client.gui.components;

import java.util.ArrayList;
import java.util.List;

public class ToolbarSection {
    public final String name;
    public final List<ToolbarWidget> widgets = new ArrayList<>();

    public ToolbarSection(String name) {
        this.name = name;
    }

    public void addWidget(Object widget, int width) {
        widgets.add(new ToolbarWidget(widget, width));
    }

    public int getTotalWidth(int gap) {
        int width = 0;
        for (int i = 0; i < widgets.size(); i++) {
            if (i > 0) width += gap;
            width += widgets.get(i).getWidth();
        }
        return width;
    }
}

