package bookeditor.client.gui.components;

import bookeditor.client.gui.widget.button.IconButton;
import bookeditor.client.gui.widget.button.ColorPickerDropdown;
import bookeditor.client.gui.widget.field.NumericTextField;

public class ToolbarWidget {
    private final Object widget;
    private final int width;

    public ToolbarWidget(Object widget, int width) {
        this.widget = widget;
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public void setVisible(boolean visible) {
        if (widget instanceof IconButton) {
            ((IconButton) widget).visible = visible;
        } else if (widget instanceof NumericTextField) {
            ((NumericTextField) widget).visible = visible;
        } else if (widget instanceof ColorPickerDropdown) {
            ((ColorPickerDropdown) widget).visible = visible;
        }
    }

    public void setPosition(int x, int y) {
        if (widget instanceof IconButton) {
            ((IconButton) widget).setX(x);
            ((IconButton) widget).setY(y);
        } else if (widget instanceof NumericTextField) {
            ((NumericTextField) widget).setX(x);
            ((NumericTextField) widget).setY(y);
        } else if (widget instanceof ColorPickerDropdown) {
            ((ColorPickerDropdown) widget).setX(x);
            ((ColorPickerDropdown) widget).setY(y);
        }
    }

    public Object getWidget() {
        return widget;
    }
}

