package bookeditor.client.gui.components;

import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.widget.button.ColorPickerDropdown;
import bookeditor.client.gui.widget.editor.EditorWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class CanvasSectionBuilder implements ToolbarSectionBuilder {
    @Override
    public SectionBuildResult build(WidgetHost host, EditorWidget editor, Runnable onDirty,
                                    IntSupplier getCanvasColor, Consumer<Integer> setCanvasColor,
                                    Runnable openInsertDialog, Runnable createNewPage,
                                    Runnable deleteCurrentPage, Runnable signAction, int btnH) {
        ToolbarSection section = new ToolbarSection("Canvas");

        ColorPickerDropdown canvasColor = new ColorPickerDropdown(0, 0, argb -> {
            setCanvasColor.accept(argb);
            editor.markSnapshot();
            onDirty.run();
        }, getCanvasColor.getAsInt());
        canvasColor.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.translatable("tooltip.bookeditor.canvas_color")));
        host.addDrawable(canvasColor);
        section.addWidget(canvasColor, 20);

        SectionBuildResult res = new SectionBuildResult(section);
        res.with("canvasColorBtn", canvasColor);
        return res;
    }
}
