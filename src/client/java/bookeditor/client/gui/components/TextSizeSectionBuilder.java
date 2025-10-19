package bookeditor.client.gui.components;

import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.widget.button.IconButton;
import bookeditor.client.gui.widget.field.NumericTextField;
import bookeditor.client.gui.widget.editor.EditorWidget;
import bookeditor.client.util.IconUtils;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class TextSizeSectionBuilder implements ToolbarSectionBuilder {
    @Override
    public SectionBuildResult build(WidgetHost host, EditorWidget editor, Runnable onDirty, IntSupplier getCanvasColor, Consumer<Integer> setCanvasColor, Runnable openInsertDialog, Runnable createNewPage, Runnable deleteCurrentPage, Runnable signAction, int btnH) {
        ToolbarSection section = new ToolbarSection("Size");

        IconButton decreaseBtn = new IconButton(0, 0, 18, btnH, IconUtils.ICON_DECREASE_SIZE, Text.translatable("tooltip.bookeditor.decrease_size"), b -> {
            float currentSize = editor.getSize();
            float newSize = Math.max(0.5f, currentSize - 0.1f);
            editor.setSize(newSize);
            editor.applyStyleToSelection();
            onDirty.run();
        });
        decreaseBtn.visible = false;
        host.addDrawable(decreaseBtn);
        section.addWidget(decreaseBtn, 18);

        NumericTextField sizeField = new NumericTextField(host.getTextRenderer(), 0, 0, 40, btnH, Text.translatable("tooltip.bookeditor.text_size"));
        sizeField.setText("1.0");
        sizeField.setOnEnterPressed(() -> {
            try {
                float s = Float.parseFloat(sizeField.getText().trim());
                s = Math.max(0.5f, Math.min(3.0f, s));
                editor.setSize(s);
                editor.applyStyleToSelection();
                sizeField.setText(String.format("%.1f", s));
                onDirty.run();
            } catch (NumberFormatException ignored) {}
        });
        host.addDrawable(sizeField);
        section.addWidget(sizeField, 40);

        IconButton increaseBtn = new IconButton(0, 0, 18, btnH, IconUtils.ICON_INCREASE_SIZE, Text.translatable("tooltip.bookeditor.increase_size"), b -> {
            float currentSize = editor.getSize();
            float newSize = Math.min(3.0f, currentSize + 0.1f);
            editor.setSize(newSize);
            editor.applyStyleToSelection();
            onDirty.run();
        });
        increaseBtn.visible = false;
        host.addDrawable(increaseBtn);
        section.addWidget(increaseBtn, 18);

        SectionBuildResult res = new SectionBuildResult(section);
        res.with("decreaseBtn", decreaseBtn).with("sizeField", sizeField).with("increaseBtn", increaseBtn);
        return res;
    }
}

