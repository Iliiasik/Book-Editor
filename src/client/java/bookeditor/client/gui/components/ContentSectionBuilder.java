package bookeditor.client.gui.components;

import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.widget.button.IconButton;
import bookeditor.client.gui.widget.button.ColorPickerDropdown;
import bookeditor.client.gui.widget.editor.EditorWidget;
import bookeditor.client.util.IconUtils;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class ContentSectionBuilder implements ToolbarSectionBuilder {
    @Override
    public SectionBuildResult build(WidgetHost host, EditorWidget editor, Runnable onDirty,
                                   IntSupplier getCanvasColor, Consumer<Integer> setCanvasColor,
                                   Runnable openInsertDialog, Runnable createNewPage,
                                   Runnable deleteCurrentPage, Runnable signAction, int btnH) {
        ToolbarSection section = new ToolbarSection("Content");

        IconButton textboxBtn = new IconButton(0, 0, 18, btnH, IconUtils.ICON_TEXTBOX, Text.translatable("tooltip.bookeditor.textbox"), b -> {
            editor.activateTextBoxTool();
            onDirty.run();
        });
        textboxBtn.visible = false;
        host.addDrawable(textboxBtn);
        section.addWidget(textboxBtn, 18);

        IconButton imageBtn = new IconButton(0, 0, 18, btnH, IconUtils.ICON_IMAGE, Text.translatable("tooltip.bookeditor.image"), b -> openInsertDialog.run());
        imageBtn.visible = false;
        host.addDrawable(imageBtn);
        section.addWidget(imageBtn, 18);

        ColorPickerDropdown textBoxBgBtn = new ColorPickerDropdown(0, 0, argb -> {
            editor.setTextBoxBgColor(argb);
            onDirty.run();
        }, 0x00FFFFFF);
        textBoxBgBtn.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.translatable("tooltip.bookeditor.textbox_bg_color")));
        host.addDrawable(textBoxBgBtn);
        section.addWidget(textBoxBgBtn, 20);

        SectionBuildResult res = new SectionBuildResult(section);
        res.with("textboxBtn", textboxBtn).with("imageBtn", imageBtn).with("textBoxBgBtn", textBoxBgBtn);
        return res;
    }
}
