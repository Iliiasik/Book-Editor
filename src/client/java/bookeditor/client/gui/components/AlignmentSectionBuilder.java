package bookeditor.client.gui.components;

import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.widget.button.IconButton;
import bookeditor.client.gui.widget.editor.EditorWidget;
import bookeditor.client.util.IconUtils;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class AlignmentSectionBuilder implements ToolbarSectionBuilder {
    @Override
    public SectionBuildResult build(WidgetHost host, EditorWidget editor, Runnable onDirty,
                                    IntSupplier getCanvasColor, Consumer<Integer> setCanvasColor,
                                    Runnable openInsertDialog, Runnable createNewPage,
                                    Runnable deleteCurrentPage, Runnable signAction, int btnH) {
        ToolbarSection section = new ToolbarSection("Align");

        IconButton left = new IconButton(0, 0, 18, btnH, IconUtils.ICON_ALIGN_LEFT, Text.translatable("tooltip.bookeditor.align_left"), b -> {
            editor.setAlignment(bookeditor.data.BookData.ALIGN_LEFT);
            onDirty.run();
        });
        left.visible = false;
        host.addDrawable(left);
        section.addWidget(left, 18);

        IconButton center = new IconButton(0, 0, 18, btnH, IconUtils.ICON_ALIGN_CENTER, Text.translatable("tooltip.bookeditor.align_center"), b -> {
            editor.setAlignment(bookeditor.data.BookData.ALIGN_CENTER);
            onDirty.run();
        });
        center.visible = false;
        host.addDrawable(center);
        section.addWidget(center, 18);

        IconButton right = new IconButton(0, 0, 18, btnH, IconUtils.ICON_ALIGN_RIGHT, Text.translatable("tooltip.bookeditor.align_right"), b -> {
            editor.setAlignment(bookeditor.data.BookData.ALIGN_RIGHT);
            onDirty.run();
        });
        right.visible = false;
        host.addDrawable(right);
        section.addWidget(right, 18);

        SectionBuildResult res = new SectionBuildResult(section);
        res.with("leftBtn", left).with("centerBtn", center).with("rightBtn", right);
        return res;
    }
}

