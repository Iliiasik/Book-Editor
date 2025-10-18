package bookeditor.client.gui.components;

import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.widget.button.IconButton;
import bookeditor.client.gui.widget.editor.EditorWidget;
import bookeditor.client.util.IconUtils;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class PageManagementSectionBuilder implements ToolbarSectionBuilder {
    @Override
    public SectionBuildResult build(WidgetHost host, EditorWidget editor, Runnable onDirty,
                                    IntSupplier getCanvasColor, Consumer<Integer> setCanvasColor,
                                    Runnable openInsertDialog, Runnable createNewPage,
                                    Runnable deleteCurrentPage, Runnable signAction, int btnH) {
        ToolbarSection section = new ToolbarSection("Pages");

        IconButton newPage = new IconButton(0, 0, 18, btnH, IconUtils.ICON_NEW_PAGE, Text.translatable("tooltip.bookeditor.new_page"), b -> createNewPage.run());
        newPage.visible = false;
        host.addDrawable(newPage);
        section.addWidget(newPage, 18);

        IconButton deletePage = new IconButton(0, 0, 18, btnH, IconUtils.ICON_DELETE_PAGE, Text.translatable("tooltip.bookeditor.delete_page"), b -> deleteCurrentPage.run());
        deletePage.visible = false;
        host.addDrawable(deletePage);
        section.addWidget(deletePage, 18);

        IconButton sign = new IconButton(0, 0, 18, btnH, IconUtils.ICON_SIGN, Text.translatable("tooltip.bookeditor.sign"), b -> signAction.run());
        sign.visible = false;
        host.addDrawable(sign);
        section.addWidget(sign, 18);

        SectionBuildResult res = new SectionBuildResult(section);
        res.with("newPageBtn", newPage).with("deletePageBtn", deletePage).with("signBtn", sign);
        return res;
    }
}
