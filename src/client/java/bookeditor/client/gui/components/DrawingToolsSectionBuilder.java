package bookeditor.client.gui.components;

import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.widget.button.IconButton;
import bookeditor.client.gui.widget.button.ColorPickerDropdown;
import bookeditor.client.gui.widget.field.NumericTextField;
import bookeditor.client.gui.widget.editor.EditorWidget;
import bookeditor.client.util.IconUtils;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class DrawingToolsSectionBuilder implements ToolbarSectionBuilder {
    @Override
    public SectionBuildResult build(WidgetHost host, EditorWidget editor, Runnable onDirty,
                                    IntSupplier getCanvasColor, Consumer<Integer> setCanvasColor,
                                    Runnable openInsertDialog, Runnable createNewPage,
                                    Runnable deleteCurrentPage, Runnable signAction, int btnH) {
        ToolbarSection section = new ToolbarSection("Drawing");

        IconButton brush = new IconButton(0, 0, 18, btnH, IconUtils.ICON_BRUSH, Text.translatable("tooltip.bookeditor.brush"), b -> {
        });
        brush.visible = false;
        host.addDrawable(brush);
        section.addWidget(brush, 18);

        IconButton spray = new IconButton(0, 0, 18, btnH, IconUtils.ICON_SPRAY, Text.translatable("tooltip.bookeditor.spray"), b -> {
        });
        spray.visible = false;
        host.addDrawable(spray);
        section.addWidget(spray, 18);

        IconButton line = new IconButton(0, 0, 18, btnH, IconUtils.ICON_LINE, Text.translatable("tooltip.bookeditor.line"), b -> {
        });
        line.visible = false;
        host.addDrawable(line);
        section.addWidget(line, 18);

        IconButton rectangle = new IconButton(0, 0, 18, btnH, IconUtils.ICON_RECTANGLE, Text.translatable("tooltip.bookeditor.rectangle"), b -> {
        });
        rectangle.visible = false;
        host.addDrawable(rectangle);
        section.addWidget(rectangle, 18);

        IconButton circle = new IconButton(0, 0, 18, btnH, IconUtils.ICON_CIRCLE, Text.translatable("tooltip.bookeditor.circle"), b -> {
        });
        circle.visible = false;
        host.addDrawable(circle);
        section.addWidget(circle, 18);

        IconButton eraser = new IconButton(0, 0, 18, btnH, IconUtils.ICON_ERASER, Text.translatable("tooltip.bookeditor.eraser"), b -> {
        });
        eraser.visible = false;
        host.addDrawable(eraser);
        section.addWidget(eraser, 18);

        ColorPickerDropdown toolColor = new ColorPickerDropdown(0, 0, argb -> {
            editor.setDrawingToolColor(argb);
            onDirty.run();
        }, 0xFF000000);
        toolColor.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.translatable("tooltip.bookeditor.tool_color")));
        host.addDrawable(toolColor);
        section.addWidget(toolColor, 20);

        NumericTextField toolSize = new NumericTextField(host.getTextRenderer(), 0, 0, 36, btnH, Text.translatable("tooltip.bookeditor.tool_size"));
        toolSize.setText("3");
        toolSize.setOnEnterPressed(() -> {
            try {
                int px = Integer.parseInt(toolSize.getText().trim());
                px = Math.max(1, Math.min(50, px));
                editor.setToolSize(px);
                toolSize.setText(String.valueOf(px));
            } catch (Exception ignored) {}
        });
        host.addDrawable(toolSize);
        section.addWidget(toolSize, 36);

        SectionBuildResult res = new SectionBuildResult(section);
        res.with("brushBtn", brush)
           .with("sprayBtn", spray)
           .with("lineBtn", line)
           .with("rectangleBtn", rectangle)
           .with("circleBtn", circle)
           .with("eraserBtn", eraser)
           .with("toolColorBtn", toolColor)
           .with("toolSizeField", toolSize);
        return res;
    }
}
