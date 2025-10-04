package bookeditor.client.gui.base;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;

public interface WidgetHost {
    <T extends Element & Drawable & Selectable> T addDrawable(T widget);
    TextRenderer getTextRenderer();
}