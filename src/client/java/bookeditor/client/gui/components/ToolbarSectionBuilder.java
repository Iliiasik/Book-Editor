package bookeditor.client.gui.components;

import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.widget.editor.EditorWidget;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

public interface ToolbarSectionBuilder {
    SectionBuildResult build(WidgetHost host,
                         EditorWidget editor,
                         Runnable onDirty,
                         IntSupplier getCanvasColor,
                         Consumer<Integer> setCanvasColor,
                         Runnable openInsertDialog,
                         Runnable createNewPage,
                         Runnable deleteCurrentPage,
                         Runnable signAction,
                         int btnH);
}
