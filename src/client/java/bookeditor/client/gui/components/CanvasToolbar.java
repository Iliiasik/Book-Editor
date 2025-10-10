package bookeditor.client.gui.components;

import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.widget.ColorPickerButton;
import bookeditor.client.gui.widget.RichTextEditorWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class CanvasToolbar {
    private final WidgetHost host;
    private final RichTextEditorWidget editor;
    private final Runnable openInsertDialog;
    private final Runnable createNewPage;
    private final Runnable deleteCurrentPage;
    private final Runnable signAction;
    private final Runnable onDirty;
    private final Consumer<Integer> onCanvasColorChanged;
    private final int x;
    private final int y;
    private final int btnH;
    private final int gap;

    private ButtonWidget textBoxBtn;
    private ButtonWidget imgBtn;
    private ColorPickerButton canvasColorBtn;
    private ButtonWidget brushToggleBtn;
    private TextFieldWidget brushSizeField;
    private ButtonWidget brushApplyBtn;
    private ButtonWidget newPageBtn, deletePageBtn;
    private ButtonWidget signBtn;

    public CanvasToolbar(WidgetHost host,
                         RichTextEditorWidget editor,
                         Runnable openInsertDialog,
                         Runnable createNewPage,
                         Runnable deleteCurrentPage,
                         Runnable signAction,
                         Runnable onDirty,
                         Consumer<Integer> onCanvasColorChanged,
                         int x, int y, int btnH, int gap) {
        this.host = host;
        this.editor = editor;
        this.openInsertDialog = openInsertDialog;
        this.createNewPage = createNewPage;
        this.deleteCurrentPage = deleteCurrentPage;
        this.signAction = signAction;
        this.onDirty = onDirty;
        this.onCanvasColorChanged = onCanvasColorChanged;
        this.x = x;
        this.y = y;
        this.btnH = btnH;
        this.gap = gap;
    }

    public void build(int initialCanvasArgb) {
        int cx = x;

        textBoxBtn = addBtn(Text.translatable("screen.bookeditor.textbox").getString(), b -> {
            editor.insertTextBox();
            onDirty.run();
        }, cx, 60);
        cx += 60 + gap;

        imgBtn = addBtn(Text.translatable("screen.bookeditor.img").getString(),
                b -> openInsertDialog.run(), cx, 48);
        cx += 48 + gap;

        canvasColorBtn = new ColorPickerButton(cx, y, argb -> {
            onCanvasColorChanged.accept(argb);
            editor.markSnapshot();
            onDirty.run();
        }, initialCanvasArgb);
        canvasColorBtn.setWidth(48);
        host.addDrawable(canvasColorBtn);
        cx += 48 + gap;

        brushToggleBtn = addBtn(Text.translatable("screen.bookeditor.brush").getString(), b -> {
            boolean newState = !editor.isBrushMode();
            editor.setBrushMode(newState);
            brushToggleBtn.setMessage(Text.translatable(newState ? "screen.bookeditor.brush_on" : "screen.bookeditor.brush"));
        }, cx, 56);
        cx += 56 + gap;

        brushSizeField = new TextFieldWidget(host.getTextRenderer(), cx, y, 36, btnH, Text.translatable("screen.bookeditor.brush_size"));
        brushSizeField.setText("3");
        host.addDrawable(brushSizeField);
        cx += 36 + gap;

        brushApplyBtn = addBtn(Text.translatable("screen.bookeditor.apply").getString(), b -> {
            try {
                int px = Math.max(1, Math.min(32, Integer.parseInt(brushSizeField.getText().trim())));
                editor.setBrushSize(px);
            } catch (Exception ignored) {
            }
        }, cx, 40);
        cx += 40 + gap;

        newPageBtn = addBtn(Text.translatable("screen.bookeditor.new_page").getString(), b -> createNewPage.run(), cx, 56);
        cx += 56 + gap;

        deletePageBtn = addBtn(Text.translatable("screen.bookeditor.delete_page").getString(), b -> deleteCurrentPage.run(), cx, 56);
        cx += 56 + gap;

        signBtn = addBtn(Text.translatable("screen.bookeditor.sign").getString(), b -> signAction.run(), cx, 56);
    }

    public void setVisible(boolean v, boolean signed) {
        if (textBoxBtn != null) textBoxBtn.visible = v;
        if (imgBtn != null) imgBtn.visible = v;
        if (canvasColorBtn != null) canvasColorBtn.visible = v;
        if (brushToggleBtn != null) brushToggleBtn.visible = v;
        if (brushSizeField != null) brushSizeField.visible = v;
        if (brushApplyBtn != null) brushApplyBtn.visible = v;
        if (newPageBtn != null) newPageBtn.visible = v;
        if (deletePageBtn != null) deletePageBtn.visible = v;
        if (signBtn != null) signBtn.visible = v && !signed;
    }

    public void setCanvasColor(int argb) {
        if (canvasColorBtn != null) canvasColorBtn.setArgb(argb);
    }

    public void setBrushSize(int px) {
        if (brushSizeField != null) brushSizeField.setText(Integer.toString(px));
        editor.setBrushSize(px);
    }

    private ButtonWidget addBtn(String label, ButtonWidget.PressAction onPress, int cx, int w) {
        ButtonWidget btn = ButtonWidget.builder(Text.literal(label), onPress).dimensions(cx, y, w, btnH).build();
        host.addDrawable(btn);
        return btn;
    }
}