package bookeditor.client.gui.widget;

import bookeditor.client.editor.brush.BrushTool;
import bookeditor.client.editor.history.HistoryManager;
import bookeditor.client.editor.image.ImageInteraction;
import bookeditor.client.editor.input.EditorInputHandler;
import bookeditor.client.editor.input.EditorMouseHandler;
import bookeditor.client.editor.mode.EditorMode;
import bookeditor.client.editor.render.EditorRenderer;
import bookeditor.client.editor.text.StyleParams;
import bookeditor.client.editor.textbox.TextBoxCaret;
import bookeditor.client.editor.textbox.TextBoxEditOps;
import bookeditor.client.editor.textbox.TextBoxInteraction;
import bookeditor.client.editor.textbox.TextBoxRenderer;
import bookeditor.data.BookData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class RichTextEditorWidget extends ClickableWidget {
    private static final int PAD_OUT = 8;
    private static final int PAD_IN = 8;
    private static final int LOGICAL_W = 960;
    private static final int LOGICAL_H = 600;
    private static final int MAX_TEXTBOX_CHARS = 5000;

    private final TextRenderer textRenderer;
    private final java.util.function.Consumer<String> onImageUrlSeen;
    private final Runnable onDirty;

    private boolean editable;
    private BookData.Page page;
    private EditorMode mode = EditorMode.OBJECT_MODE;

    private boolean bold;
    private boolean italic;
    private boolean underline;
    private int argb = 0xFF202020;
    private float size = 1.0f;

    private final TextBoxCaret textBoxCaret = new TextBoxCaret();
    private final BrushTool brushTool = new BrushTool();
    private final ImageInteraction imageInteraction = new ImageInteraction();
    private final TextBoxInteraction textBoxInteraction = new TextBoxInteraction();
    private final HistoryManager history = new HistoryManager();

    private final TextBoxRenderer textBoxRenderer = new TextBoxRenderer();
    private final TextBoxEditOps textBoxOps = new TextBoxEditOps();
    private final EditorRenderer editorRenderer = new EditorRenderer();
    private final EditorInputHandler inputHandler = new EditorInputHandler();
    private final EditorMouseHandler mouseHandler = new EditorMouseHandler();

    private int scrollY = 0;

    public RichTextEditorWidget(TextRenderer textRenderer, int x, int y, int width, int height,
                                boolean editable, java.util.function.Consumer<String> onImageUrlSeen, Runnable onDirty) {
        super(x, y, width, height, Text.literal("RichEditor"));
        this.textRenderer = textRenderer;
        this.editable = editable;
        this.onImageUrlSeen = onImageUrlSeen;
        this.onDirty = onDirty;
        this.active = editable;
    }

    public void setHeight(int height) {
        this.height = Math.max(40, height);
    }

    public void setBounds(int x, int y, int width, int height) {
        this.setX(x);
        this.setY(y);
        this.setWidth(width);
        this.height = Math.max(40, height);
    }

    private int innerLeft() {
        return getX() + PAD_OUT;
    }

    private int innerTop() {
        return getY() + PAD_OUT;
    }

    private int innerW() {
        return Math.max(0, getWidth() - PAD_OUT * 2);
    }

    private int innerH() {
        return Math.max(0, getHeight() - PAD_OUT * 2);
    }

    private double scale() {
        double sw = (double) innerW() / (LOGICAL_W + PAD_IN * 2.0);
        double sh = (double) innerH() / (LOGICAL_H + PAD_IN * 2.0);
        return Math.max(0.1, Math.min(sw, sh));
    }

    private int canvasScreenLeft() {
        int scaledW = (int) Math.floor(scale() * (LOGICAL_W + PAD_IN * 2));
        return innerLeft() + Math.max(0, (innerW() - scaledW) / 2);
    }

    private int canvasScreenTop() {
        int scaledH = (int) Math.floor(scale() * (LOGICAL_H + PAD_IN * 2));
        return innerTop() + Math.max(0, (innerH() - scaledH) / 2);
    }

    private int contentScreenLeft() {
        return canvasScreenLeft() + (int) Math.round(scale() * PAD_IN);
    }

    private int contentScreenTop() {
        return canvasScreenTop() + (int) Math.round(scale() * PAD_IN);
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        this.active = editable;
    }

    public void setContent(BookData.Page page) {
        this.page = page;
        mode = EditorMode.OBJECT_MODE;
        textBoxCaret.reset();
        textBoxInteraction.clearSelection();
        imageInteraction.clearSelection();
        scrollY = 0;
        if (onImageUrlSeen != null && page != null) {
            for (BookData.Node n : page.nodes) {
                if (n instanceof BookData.ImageNode img && img.url != null && !img.url.isEmpty()) {
                    onImageUrlSeen.accept(img.url);
                }
            }
        }
        history.clear();
        pushSnapshot();
    }

    public void markSnapshot() {
        pushSnapshot();
    }

    public boolean undo() {
        return history.undo(this::applySnapshot);
    }

    public boolean redo() {
        return history.redo(this::applySnapshot);
    }

    private void pushSnapshot() {
        if (page == null) return;
        history.pushSnapshot(page::toNbt);
    }

    private void pushSnapshotOnce() {
        if (page != null) history.pushSnapshotOnce(page::toNbt);
    }

    private void applySnapshot(net.minecraft.nbt.NbtCompound snap) {
        BookData.Page restored = BookData.Page.fromNbt(snap.copy());
        page.nodes.clear();
        page.strokes.clear();
        page.bgArgb = restored.bgArgb;
        page.nodes.addAll(restored.nodes);
        page.strokes.addAll(restored.strokes);
        imageInteraction.clearSelection();
        textBoxInteraction.clearSelection();
        textBoxCaret.clearSelection();
        mode = EditorMode.OBJECT_MODE;
        if (onImageUrlSeen != null) {
            for (BookData.Node n : page.nodes) {
                if (n instanceof BookData.ImageNode img && img.url != null && !img.url.isEmpty()) {
                    onImageUrlSeen.accept(img.url);
                }
            }
        }
    }

    private void notifyDirty() {
        history.resetSnapshotArmed();
        if (onDirty != null) onDirty.run();
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isBold() {
        return bold;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setUnderline(boolean underline) {
        this.underline = underline;
    }

    public boolean isUnderline() {
        return underline;
    }

    public void setColor(int argb) {
        this.argb = argb;
        brushTool.setBrushColor(argb);
    }

    public void setSize(float size) {
        this.size = size;
    }

    private StyleParams style() {
        return new StyleParams(bold, italic, underline, argb, size);
    }

    public boolean isBrushMode() {
        return brushTool.isBrushMode();
    }

    public void setBrushMode(boolean brushMode) {
        brushTool.setBrushMode(brushMode);
        if (brushMode) {
            mode = EditorMode.OBJECT_MODE;
            textBoxInteraction.clearSelection();
        }
    }

    public void setBrushColor(int argb) {
        brushTool.setBrushColor(argb);
    }

    public void setBrushSize(int px) {
        brushTool.setBrushSize(px);
    }

    public void setAlignment(int align) {
        if (!editable || page == null || mode != EditorMode.TEXT_MODE) return;
        if (textBoxInteraction.getSelectedTextBoxIndex() < 0) return;

        var node = page.nodes.get(textBoxInteraction.getSelectedTextBoxIndex());
        if (!(node instanceof BookData.TextBoxNode box)) return;

        pushSnapshotOnce();

        for (BookData.TextSegment seg : box.segments) {
            seg.align = align;
        }

        notifyDirty();
    }

    public void insertImage(String url, int w, int h, boolean gif) {
        if (!editable || page == null) return;
        pushSnapshotOnce();
        int maxImgW = Math.max(8, LOGICAL_W);
        BookData.ImageNode img = new BookData.ImageNode(url, Math.max(8, Math.min(w, maxImgW)), Math.max(8, h), gif);
        img.absolute = true;
        img.x = 0;
        img.y = Math.max(0, Math.min(scrollY + 10, Math.max(0, LOGICAL_H - img.h)));
        if (onImageUrlSeen != null && url != null && !url.isEmpty()) onImageUrlSeen.accept(url);
        page.nodes.add(img);
        mode = EditorMode.OBJECT_MODE;
        notifyDirty();
    }

    public void insertTextBox() {
        if (!editable || page == null) return;
        pushSnapshotOnce();
        BookData.TextBoxNode box = new BookData.TextBoxNode(50, 50 + scrollY, 300, 100);
        box.setText("", bold, italic, underline, argb, size);
        page.nodes.add(box);
        mode = EditorMode.OBJECT_MODE;
        notifyDirty();
    }

    public void applyStyleToSelection() {
        if (!editable || page == null || mode != EditorMode.TEXT_MODE) return;
        if (textBoxInteraction.getSelectedTextBoxIndex() < 0) return;

        var node = page.nodes.get(textBoxInteraction.getSelectedTextBoxIndex());
        if (!(node instanceof BookData.TextBoxNode box)) return;

        if (textBoxCaret.hasSelection()) {
            pushSnapshotOnce();
            textBoxOps.applyStyleToSelection(box, textBoxCaret, style());
            notifyDirty();
        }
    }

    @Override
    protected void renderButton(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderFrame(ctx);
        renderCanvas(ctx);
        enableScissor(ctx);
        editorRenderer.render(ctx, page, mode, brushTool, imageInteraction, textBoxInteraction, textBoxCaret,
                textRenderer, this.isFocused(), editable, contentScreenLeft(), contentScreenTop(),
                canvasScreenTop(), scale(), scrollY, LOGICAL_W, LOGICAL_H);
        ctx.disableScissor();
    }

    private void renderFrame(DrawContext ctx) {
        int frame = isHovered() ? 0xFFAAAAAA : 0xFFBEBEBE;
        ctx.fill(getX() - 1, getY() - 1, getX() + getWidth() + 1, getY() + getHeight() + 1, frame);
        ctx.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xFFEFEFEF);
    }

    private void renderCanvas(DrawContext ctx) {
        int cLeft = canvasScreenLeft();
        int cTop = canvasScreenTop();
        int scaledW = (int) Math.floor(scale() * (LOGICAL_W + PAD_IN * 2));
        int scaledH = (int) Math.floor(scale() * (LOGICAL_H + PAD_IN * 2));
        int bg = page != null ? page.bgArgb : 0xFFF8F8F8;
        ctx.fill(cLeft, cTop, cLeft + scaledW, cTop + scaledH, bg);
        ctx.fill(cLeft - 1, cTop - 1, cLeft + scaledW + 1, cTop, 0x33000000);
        ctx.fill(cLeft - 1, cTop + scaledH, cLeft + scaledW + 1, cTop + scaledH + 1, 0x33000000);
        ctx.fill(cLeft - 1, cTop, cLeft, cTop + scaledH, 0x33000000);
        ctx.fill(cLeft + scaledW, cTop, cLeft + scaledW + 1, cTop + scaledH, 0x33000000);
    }

    private void enableScissor(DrawContext ctx) {
        int cLeft = canvasScreenLeft();
        int cTop = canvasScreenTop();
        int scaledW = (int) Math.floor(scale() * (LOGICAL_W + PAD_IN * 2));
        int scaledH = (int) Math.floor(scale() * (LOGICAL_H + PAD_IN * 2));
        int scLeft = Math.max(innerLeft(), cLeft);
        int scTop = Math.max(innerTop(), cTop);
        int scRight = Math.min(innerLeft() + innerW(), cLeft + scaledW);
        int scBottom = Math.min(innerTop() + innerH(), cTop + scaledH);
        if (scRight > scLeft && scBottom > scTop) ctx.enableScissor(scLeft, scTop, scRight, scBottom);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isMouseOver(mouseX, mouseY)) return false;
        this.setFocused(true);

        mode = mouseHandler.handleMouseClick((int) mouseX, (int) mouseY, editable, page, mode,
                brushTool, imageInteraction, textBoxInteraction, textBoxCaret, textBoxRenderer,
                textRenderer, contentScreenLeft(), contentScreenTop(), scale(), scrollY, this::pushSnapshotOnce);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        return mouseHandler.handleMouseDrag((int) mouseX, (int) mouseY, mode, editable, brushTool,
                imageInteraction, textBoxInteraction, textBoxCaret, textBoxRenderer, textRenderer,
                page, contentScreenLeft(), contentScreenTop(), scale(), scrollY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean changed = false;
        if (imageInteraction.mouseReleased()) changed = true;
        if (textBoxInteraction.mouseReleased()) changed = true;
        if (brushTool.endStrokeIfActive(page)) changed = true;
        if (changed) notifyDirty();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!editable || page == null || !this.isFocused()) return false;
        if (brushTool.isBrushMode()) return false;

        int selectedIdx = textBoxInteraction.getSelectedTextBoxIndex();
        if (selectedIdx >= 0 && selectedIdx < page.nodes.size()) {
            var node = page.nodes.get(selectedIdx);
            if (node instanceof BookData.TextBoxNode box) {
                if (box.getFullText().length() >= MAX_TEXTBOX_CHARS) {
                    return false;
                }
            }
        }

        pushSnapshotOnce();
        boolean handled = inputHandler.handleCharTyped(mode, page, textBoxInteraction, textBoxCaret, style(), chr);
        if (handled) notifyDirty();
        return handled;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isFocused()) return false;

        if ((keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) && page != null) {
            if (mode == EditorMode.OBJECT_MODE) {
                if (imageInteraction.getSelectedImageIndex() >= 0) {
                    pushSnapshotOnce();
                    imageInteraction.deleteSelectedIfImage(page);
                    notifyDirty();
                    return true;
                }
                if (textBoxInteraction.getSelectedTextBoxIndex() >= 0) {
                    pushSnapshotOnce();
                    textBoxInteraction.deleteSelectedIfTextBox(page);
                    notifyDirty();
                    return true;
                }
            }
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE && mode == EditorMode.TEXT_MODE) {
            mode = EditorMode.OBJECT_MODE;
            textBoxInteraction.setEditingText(false);
            textBoxCaret.clearSelection();
            return true;
        }

        if (!editable || page == null || brushTool.isBrushMode()) return false;

        pushSnapshotOnce();
        boolean handled = inputHandler.handleKeyPressed(mode, page, textBoxInteraction, textBoxCaret, keyCode, modifiers);
        if (handled) notifyDirty();
        return handled;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}