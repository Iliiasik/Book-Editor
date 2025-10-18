package bookeditor.client.gui.components;

import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.widget.button.IconButton;
import bookeditor.client.gui.widget.editor.EditorWidget;
import bookeditor.client.util.IconUtils;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

@SuppressWarnings("unused")
public class HistorySectionBuilder implements ToolbarSectionBuilder {
    @Override
    public SectionBuildResult build(WidgetHost host, EditorWidget editor, Runnable onDirty,
                                IntSupplier getCanvasColor, Consumer<Integer> setCanvasColor,
                                Runnable openInsertDialog, Runnable createNewPage,
                                Runnable deleteCurrentPage, Runnable signAction, int btnH) {
        ToolbarSection section = new ToolbarSection("History");

        IconButton undo = new IconButton(0, 0, 18, btnH, IconUtils.ICON_UNDO, Text.translatable("tooltip.bookeditor.undo"), b -> {
            if (editor.undo()) onDirty.run();
        });
        undo.visible = false;
        host.addDrawable(undo);
        section.addWidget(undo, 18);

        IconButton redo = new IconButton(0, 0, 18, btnH, IconUtils.ICON_REDO, Text.translatable("tooltip.bookeditor.redo"), b -> {
            if (editor.redo()) onDirty.run();
        });
        redo.visible = false;
        host.addDrawable(redo);
        section.addWidget(redo, 18);

        SectionBuildResult res = new SectionBuildResult(section);
        res.with("undoBtn", undo).with("redoBtn", redo);
        return res;
    }
}
