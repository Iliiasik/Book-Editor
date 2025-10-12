package bookeditor.client.gui.components;

import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.widget.ColorPickerDropdown;
import bookeditor.client.gui.widget.IconButton;
import bookeditor.client.gui.widget.NumericTextField;
import bookeditor.client.gui.widget.RichTextEditorWidget;
import bookeditor.client.util.IconUtils;
import bookeditor.data.BookData;
import net.minecraft.client.gui.DrawContext;
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
    private NumericTextField sizeField;
    private IconButton alignLeftBtn, alignCenterBtn, alignRightBtn;
    private ColorPickerDropdown textColorBtn;

    private int historyEndX;
    private int stylingEndX;
    private int sizeEndX;
    private int alignEndX;
    private int colorEndX;

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
        cx += 18 + 10;
        historyEndX = cx;

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
        cx += 18 + 10;
        stylingEndX = cx;

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

        sizeField = new NumericTextField(host.getTextRenderer(), cx, y, 40, btnH, Text.translatable("tooltip.bookeditor.text_size"));
        sizeField.setText("1.0");
        sizeField.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.translatable("tooltip.bookeditor.text_size_range")));
        sizeField.setOnEnterPressed(() -> {
            try {
                float size = Float.parseFloat(sizeField.getText().trim());
                size = Math.max(0.5f, Math.min(3.0f, size));
                editor.setSize(size);
                editor.applyStyleToSelection();
                sizeField.setText(String.format("%.1f", size));
                onDirty.run();
            } catch (NumberFormatException ignored) {
            }
        });
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
        cx += 18 + 10;
        sizeEndX = cx;

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
        cx += 18 + 10;
        alignEndX = cx;

        textColorBtn = new ColorPickerDropdown(cx, y, argb -> {
            editor.setColor(argb);
            editor.applyStyleToSelection();
            onDirty.run();
        }, 0xFF202020);
        textColorBtn.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.translatable("tooltip.bookeditor.text_color")));
        host.addDrawable(textColorBtn);
        cx += 20;
        colorEndX = cx;

        refreshFormatButtons();
    }

    public void renderSectionHeaders(DrawContext ctx, int textColor) {
        int labelY = y - 14;

        ctx.drawText(host.getTextRenderer(), Text.translatable("toolbar.bookeditor.history"),
                x, labelY, textColor, false);

        ctx.drawText(host.getTextRenderer(), Text.translatable("toolbar.bookeditor.styling"),
                historyEndX, labelY, textColor, false);

        ctx.drawText(host.getTextRenderer(), Text.translatable("toolbar.bookeditor.size"),
                stylingEndX, labelY, textColor, false);

        ctx.drawText(host.getTextRenderer(), Text.translatable("toolbar.bookeditor.alignment"),
                sizeEndX, labelY, textColor, false);

        ctx.drawText(host.getTextRenderer(), Text.translatable("toolbar.bookeditor.color"),
                alignEndX, labelY, textColor, false);
    }

    public void renderSectionBoxes(DrawContext ctx) {
        int boxY = y - 2;
        int boxH = btnH + 4;

        ctx.fill(x - 2, boxY, historyEndX - 5, boxY + boxH, 0x33FFFFFF);
        ctx.fill(historyEndX - 2, boxY, stylingEndX - 5, boxY + boxH, 0x33FFFFFF);
        ctx.fill(stylingEndX - 2, boxY, sizeEndX - 5, boxY + boxH, 0x33FFFFFF);
        ctx.fill(sizeEndX - 2, boxY, alignEndX - 5, boxY + boxH, 0x33FFFFFF);
        ctx.fill(alignEndX - 2, boxY, colorEndX + 2, boxY + boxH, 0x33FFFFFF);
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
}