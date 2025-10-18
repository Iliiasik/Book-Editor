package bookeditor.client.gui.widget.editor;

import bookeditor.client.editor.interaction.ImageInteraction;
import bookeditor.client.editor.input.EditorInputHandler;
import bookeditor.client.editor.input.EditorMouseHandler;
import bookeditor.client.editor.mode.EditorMode;
import bookeditor.client.editor.render.EditorRenderer;
import bookeditor.client.editor.textbox.TextBoxCaret;
import bookeditor.client.editor.tools.TextBoxCreationTool;
import bookeditor.client.editor.textbox.TextBoxEditOps;
import bookeditor.client.editor.interaction.TextBoxInteraction;
import bookeditor.client.editor.render.TextBoxRenderer;
import bookeditor.client.editor.tools.AdvancedDrawingTool;
import bookeditor.client.editor.tools.EraserTool;
import bookeditor.data.BookData;
import net.minecraft.client.font.TextRenderer;
import java.util.function.Consumer;

public class EditorState {
    public static final int PAD_OUT = 8;
    public static final int PAD_IN = 8;
    public static final int LOGICAL_W = 960;
    public static final int LOGICAL_H = 600;
    public static final int MAX_TEXTBOX_CHARS = 5000;
    public final TextRenderer textRenderer;
    public final Consumer<String> onImageUrlSeen;
    public final Runnable onDirty;
    public boolean editable;
    public BookData.Page page;
    public EditorMode mode = EditorMode.OBJECT_MODE;
    public boolean bold;
    public boolean italic;
    public boolean underline;
    public int argb = 0xFF202020;
    public float size = 1.0f;
    public int textBoxBgColor = 0x00FFFFFF;
    public int scrollY = 0;
    public String clipboard = "";
    public final TextBoxCaret textBoxCaret = new TextBoxCaret();
    public final ImageInteraction imageInteraction = new ImageInteraction();
    public final TextBoxInteraction textBoxInteraction = new TextBoxInteraction();
    public final TextBoxCreationTool textBoxCreationTool = new TextBoxCreationTool();
    public final EraserTool eraserTool = new EraserTool();
    public final AdvancedDrawingTool drawingTool = new AdvancedDrawingTool();
    public final TextBoxRenderer textBoxRenderer = new TextBoxRenderer();
    public final TextBoxEditOps textBoxOps = new TextBoxEditOps();
    public final EditorRenderer editorRenderer = new EditorRenderer();
    public final EditorInputHandler inputHandler = new EditorInputHandler();
    public final EditorMouseHandler mouseHandler = new EditorMouseHandler();
    private final EditorWidget widget;
    public EditorState(TextRenderer textRenderer, boolean editable, Consumer<String> onImageUrlSeen, Runnable onDirty, EditorWidget widget) {
        this.textRenderer = textRenderer;
        this.editable = editable;
        this.onImageUrlSeen = onImageUrlSeen;
        this.onDirty = onDirty;
        this.widget = widget;
    }
    public void setHeight(int height) {
        widget.setWidgetHeight(Math.max(40, height));
    }
    public void setEditable(boolean editable) {
        this.editable = editable;
        widget.active = editable;
    }
    public void setContent(BookData.Page page) {
        this.page = page;
        mode = EditorMode.OBJECT_MODE;
        textBoxCaret.reset();
        textBoxInteraction.clearSelection();
        imageInteraction.clearSelection();
        textBoxCreationTool.deactivate();
        drawingTool.setActive(false);
        eraserTool.setActive(false);
        scrollY = 0;
    }
    public int innerLeft() {
        return widget.getX() + PAD_OUT;
    }
    public int innerTop() {
        return widget.getY() + PAD_OUT;
    }
    public int innerW() {
        return Math.max(0, widget.getWidth() - PAD_OUT * 2);
    }
    public int innerH() {
        return Math.max(0, widget.getHeight() - PAD_OUT * 2);
    }
    public double scale() {
        double sw = (double) innerW() / (LOGICAL_W + PAD_IN * 2.0);
        double sh = (double) innerH() / (LOGICAL_H + PAD_IN * 2.0);
        return Math.max(0.1, Math.min(sw, sh));
    }
    public int canvasScreenLeft() {
        int scaledW = (int) Math.floor(scale() * (LOGICAL_W + PAD_IN * 2));
        return innerLeft() + Math.max(0, (innerW() - scaledW) / 2);
    }
    public int canvasScreenTop() {
        int scaledH = (int) Math.floor(scale() * (LOGICAL_H + PAD_IN * 2));
        return innerTop() + Math.max(0, (innerH() - scaledH) / 2);
    }
    public int contentScreenLeft() {
        return canvasScreenLeft() + (int) Math.round(scale() * PAD_IN);
    }
    public int contentScreenTop() {
        return canvasScreenTop() + (int) Math.round(scale() * PAD_IN);
    }
    public EditorWidget getWidget() {
        return widget;
    }
}