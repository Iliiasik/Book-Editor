package bookeditor.client.gui.components;

import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.widget.button.IconButton;
import bookeditor.client.gui.widget.button.ColorPickerDropdown;
import bookeditor.client.gui.widget.editor.EditorWidget;
import bookeditor.client.util.IconUtils;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class TextStyleSectionBuilder implements ToolbarSectionBuilder {
    @Override
    public SectionBuildResult build(WidgetHost host, EditorWidget editor, Runnable onDirty, IntSupplier getCanvasColor, Consumer<Integer> setCanvasColor, Runnable openInsertDialog, Runnable createNewPage, Runnable deleteCurrentPage, Runnable signAction, int btnH) {
        ToolbarSection section = new ToolbarSection("Text Style");

        IconButton bold = new IconButton(0, 0, 18, btnH, IconUtils.ICON_BOLD, Text.translatable("tooltip.bookeditor.bold"), b -> {
            editor.setBold(!editor.isBold());
            editor.applyStyleToSelection();
            onDirty.run();
        });
        bold.visible = false;
        host.addDrawable(bold);
        section.addWidget(bold, 18);

        IconButton italic = new IconButton(0, 0, 18, btnH, IconUtils.ICON_ITALIC, Text.translatable("tooltip.bookeditor.italic"), b -> {
            editor.setItalic(!editor.isItalic());
            editor.applyStyleToSelection();
            onDirty.run();
        });
        italic.visible = false;
        host.addDrawable(italic);
        section.addWidget(italic, 18);

        IconButton underline = new IconButton(0, 0, 18, btnH, IconUtils.ICON_UNDERLINE, Text.translatable("tooltip.bookeditor.underline"), b -> {
            editor.setUnderline(!editor.isUnderline());
            editor.applyStyleToSelection();
            onDirty.run();
        });
        underline.visible = false;
        host.addDrawable(underline);
        section.addWidget(underline, 18);

        ColorPickerDropdown textColor = new ColorPickerDropdown(0, 0, argb -> {
            editor.setColor(argb);
            editor.applyStyleToSelection();
            onDirty.run();
        }, 0xFF202020);
        textColor.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.translatable("tooltip.bookeditor.text_color")));
        host.addDrawable(textColor);
        section.addWidget(textColor, 20);

        SectionBuildResult res = new SectionBuildResult(section);
        res.with("boldBtn", bold)
           .with("italicBtn", italic)
           .with("underlineBtn", underline)
           .with("textColorBtn", textColor);
        return res;
    }
}
