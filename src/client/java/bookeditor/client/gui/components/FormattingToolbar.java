package bookeditor.client.gui.components;

import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.widget.ColorPickerButton;
import bookeditor.client.gui.widget.RichTextEditorWidget;
import bookeditor.data.BookData;
import net.minecraft.client.gui.widget.ButtonWidget;
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

    private ButtonWidget undoBtn, redoBtn;
    private ButtonWidget boldBtn, italicBtn, underlineBtn;
    private TextFieldWidget sizeField;
    private ButtonWidget applySizeBtn;
    private ButtonWidget alignLeftBtn, alignCenterBtn, alignRightBtn;
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

        undoBtn = addBtn("↶", b -> {
            if (editor.undo()) onDirty.run();
        }, cx);
        cx += 18 + gap;

        redoBtn = addBtn("↷", b -> {
            if (editor.redo()) onDirty.run();
        }, cx);
        cx += 18 + gap;

        boldBtn = addBtn("B", b -> {
            editor.setBold(!editor.isBold());
            editor.applyStyleToSelection();
            refreshFormatButtons();
            onDirty.run();
        }, cx);
        cx += 18 + gap;

        italicBtn = addBtn("I", b -> {
            editor.setItalic(!editor.isItalic());
            editor.applyStyleToSelection();
            refreshFormatButtons();
            onDirty.run();
        }, cx);
        cx += 18 + gap;

        underlineBtn = addBtn("U", b -> {
            editor.setUnderline(!editor.isUnderline());
            editor.applyStyleToSelection();
            refreshFormatButtons();
            onDirty.run();
        }, cx);
        cx += 18 + gap;

        sizeField = new TextFieldWidget(host.getTextRenderer(), cx, y, 40, btnH, Text.translatable("screen.bookeditor.size"));
        sizeField.setText("1.0");
        host.addDrawable(sizeField);
        cx += 40 + gap;

        applySizeBtn = addBtn(Text.translatable("screen.bookeditor.apply_size").getString(), b -> {
            try {
                float s = Math.max(0.5f, Math.min(3.0f, Float.parseFloat(sizeField.getText().trim())));
                editor.setSize(s);
                editor.applyStyleToSelection();
                onDirty.run();
            } catch (Exception ignored) {
            }
        }, cx, 46);
        cx += 46 + gap;

        alignLeftBtn = addBtn("L", b -> {
            editor.setAlignment(BookData.ALIGN_LEFT);
            onDirty.run();
        }, cx);
        cx += 18 + gap;

        alignCenterBtn = addBtn("C", b -> {
            editor.setAlignment(BookData.ALIGN_CENTER);
            onDirty.run();
        }, cx);
        cx += 18 + gap;

        alignRightBtn = addBtn("R", b -> {
            editor.setAlignment(BookData.ALIGN_RIGHT);
            onDirty.run();
        }, cx);
        cx += 18 + gap;

        textColorBtn = new ColorPickerButton(cx, y, argb -> {
            editor.setColor(argb);
            editor.setBrushColor(argb);
            editor.applyStyleToSelection();
            onDirty.run();
        }, 0xFF202020);
        textColorBtn.setWidth(18);
        host.addDrawable(textColorBtn);

        refreshFormatButtons();
    }

    private ButtonWidget addBtn(String label, ButtonWidget.PressAction onPress, int cx) {
        return addBtn(label, onPress, cx, 18);
    }

    private ButtonWidget addBtn(String label, ButtonWidget.PressAction onPress, int cx, int w) {
        ButtonWidget btn = ButtonWidget.builder(Text.literal(label), onPress).dimensions(cx, y, w, btnH).build();
        host.addDrawable(btn);
        return btn;
    }

    public void setVisible(boolean v) {
        if (undoBtn != null) undoBtn.visible = v;
        if (redoBtn != null) redoBtn.visible = v;
        if (boldBtn != null) boldBtn.visible = v;
        if (italicBtn != null) italicBtn.visible = v;
        if (underlineBtn != null) underlineBtn.visible = v;
        if (sizeField != null) sizeField.visible = v;
        if (applySizeBtn != null) applySizeBtn.visible = v;
        if (alignLeftBtn != null) alignLeftBtn.visible = v;
        if (alignCenterBtn != null) alignCenterBtn.visible = v;
        if (alignRightBtn != null) alignRightBtn.visible = v;
        if (textColorBtn != null) textColorBtn.visible = v;
    }

    public void refreshFormatButtons() {
        if (boldBtn != null) boldBtn.setMessage(Text.literal(editor.isBold() ? "[B]" : "B"));
        if (italicBtn != null) italicBtn.setMessage(Text.literal(editor.isItalic() ? "[I]" : "I"));
        if (underlineBtn != null) underlineBtn.setMessage(Text.literal(editor.isUnderline() ? "[U]" : "U"));
    }

    public void setInitialTextColor(int argb) {
        if (textColorBtn != null) textColorBtn.setArgb(argb);
        editor.setColor(argb);
        editor.setBrushColor(argb);
    }
}