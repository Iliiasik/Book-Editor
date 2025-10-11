package bookeditor.client.gui.components;

import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.widget.ColorPickerButton;
import bookeditor.client.gui.widget.IconButton;
import bookeditor.client.gui.widget.RichTextEditorWidget;
import bookeditor.client.util.IconUtils;
import bookeditor.data.BookData;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class FormattingToolbar {
    private final WidgetHost host;
    private final RichTextEditorWidget editor;
    private final Runnable onDirty;

    private final int x;
    private final int y;
    private final int btnH;
    private final int gap;

    private IconButton undoBtn, redoBtn;
    private IconButton boldBtn, italicBtn, underlineBtn;
    private IconButton increaseSizeBtn, decreaseSizeBtn;
    private TextFieldWidget sizeField;
    private IconButton alignLeftBtn, alignCenterBtn, alignRightBtn;
    private ColorPickerButton textColorBtn;

    public FormattingToolbar(WidgetHost host,
                             RichTextEditorWidget editor,
                             Runnable onDirty,
                             int x, int y, int btnH, int gap) {
        this.host = host;
        this.editor = editor;
        this.onDirty = onDirty;
        this.x = x;
        this.y = y;
        this.btnH = btnH;
        this.gap = gap;
    }

    public void build() {
        int cx = x;

        undoBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_UNDO,
                Text.translatable("tooltip.bookeditor.undo"), b -> {
            if (editor.undo()) onDirty.run();
        });
        host.addDrawable(undoBtn);
        cx += 18 + gap;

        redoBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_REDO,
                Text.translatable("tooltip.bookeditor.redo"), b -> {
            if (editor.redo()) onDirty.run();
        });
        host.addDrawable(redoBtn);
        cx += 18 + gap;

        boldBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_BOLD,
                Text.translatable("tooltip.bookeditor.bold"), b -> {
            editor.setBold(!editor.isBold());
            editor.applyStyleToSelection();
            refreshFormatButtons();
            onDirty.run();
        });
        host.addDrawable(boldBtn);
        cx += 18 + gap;

        italicBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_ITALIC,
                Text.translatable("tooltip.bookeditor.italic"), b -> {
            editor.setItalic(!editor.isItalic());
            editor.applyStyleToSelection();
            refreshFormatButtons();
            onDirty.run();
        });
        host.addDrawable(italicBtn);
        cx += 18 + gap;

        underlineBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_UNDERLINE,
                Text.translatable("tooltip.bookeditor.underline"), b -> {
            editor.setUnderline(!editor.isUnderline());
            editor.applyStyleToSelection();
            refreshFormatButtons();
            onDirty.run();
        });
        host.addDrawable(underlineBtn);
        cx += 18 + gap;

        decreaseSizeBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_DECREASE_SIZE,
                Text.translatable("tooltip.bookeditor.decrease_size"), b -> {
            float currentSize = editor.getSize();
            float newSize = Math.max(0.5f, currentSize - 0.1f);
            editor.setSize(newSize);
            editor.applyStyleToSelection();
            sizeField.setText(String.format("%.1f", newSize));
            onDirty.run();
        });
        host.addDrawable(decreaseSizeBtn);
        cx += 18 + gap;

        sizeField = new TextFieldWidget(host.getTextRenderer(), cx, y, 40, btnH, Text.translatable("tooltip.bookeditor.text_size"));
        sizeField.setText("1.0");
        sizeField.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.translatable("tooltip.bookeditor.text_size")));
        host.addDrawable(sizeField);
        cx += 40 + gap;

        increaseSizeBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_INCREASE_SIZE,
                Text.translatable("tooltip.bookeditor.increase_size"), b -> {
            float currentSize = editor.getSize();
            float newSize = Math.min(3.0f, currentSize + 0.1f);
            editor.setSize(newSize);
            editor.applyStyleToSelection();
            sizeField.setText(String.format("%.1f", newSize));
            onDirty.run();
        });
        host.addDrawable(increaseSizeBtn);
        cx += 18 + gap;

        alignLeftBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_ALIGN_LEFT,
                Text.translatable("tooltip.bookeditor.align_left"), b -> {
            editor.setAlignment(BookData.ALIGN_LEFT);
            onDirty.run();
        });
        host.addDrawable(alignLeftBtn);
        cx += 18 + gap;

        alignCenterBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_ALIGN_CENTER,
                Text.translatable("tooltip.bookeditor.align_center"), b -> {
            editor.setAlignment(BookData.ALIGN_CENTER);
            onDirty.run();
        });
        host.addDrawable(alignCenterBtn);
        cx += 18 + gap;

        alignRightBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_ALIGN_RIGHT,
                Text.translatable("tooltip.bookeditor.align_right"), b -> {
            editor.setAlignment(BookData.ALIGN_RIGHT);
            onDirty.run();
        });
        host.addDrawable(alignRightBtn);
        cx += 18 + gap;

        textColorBtn = new ColorPickerButton(cx, y, argb -> {
            editor.setColor(argb);
            editor.applyStyleToSelection();
            onDirty.run();
        }, 0xFF202020);
        textColorBtn.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.translatable("tooltip.bookeditor.text_color")));
        host.addDrawable(textColorBtn);

        refreshFormatButtons();
    }

    public void setVisible(boolean v) {
        if (undoBtn != null) undoBtn.visible = v;
        if (redoBtn != null) redoBtn.visible = v;
        if (boldBtn != null) boldBtn.visible = v;
        if (italicBtn != null) italicBtn.visible = v;
        if (underlineBtn != null) underlineBtn.visible = v;
        if (decreaseSizeBtn != null) decreaseSizeBtn.visible = v;
        if (sizeField != null) sizeField.visible = v;
        if (increaseSizeBtn != null) increaseSizeBtn.visible = v;
        if (alignLeftBtn != null) alignLeftBtn.visible = v;
        if (alignCenterBtn != null) alignCenterBtn.visible = v;
        if (alignRightBtn != null) alignRightBtn.visible = v;
        if (textColorBtn != null) textColorBtn.visible = v;
    }

    public void refreshFormatButtons() {
        if (boldBtn != null) boldBtn.setSelected(editor.isBold());
        if (italicBtn != null) italicBtn.setSelected(editor.isItalic());
        if (underlineBtn != null) underlineBtn.setSelected(editor.isUnderline());
    }

    public void setInitialTextColor(int argb) {
        if (textColorBtn != null) textColorBtn.setArgb(argb);
        editor.setColor(argb);
    }

    public void setFontSizeField(float s) {
        if (sizeField != null) sizeField.setText(String.format("%.1f", s));
    }

    public void updateTextColor(int argb) {
        if (textColorBtn != null) textColorBtn.setArgb(argb);
    }
}