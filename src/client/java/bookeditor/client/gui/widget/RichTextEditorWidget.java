package bookeditor.client.gui.widget;

import bookeditor.client.editor.brush.BrushTool;
import bookeditor.client.editor.caret.CaretSelectionModel;
import bookeditor.client.editor.history.HistoryManager;
import bookeditor.client.editor.image.ImageInteraction;
import bookeditor.client.editor.render.CaretPainter;
import bookeditor.client.editor.render.ImageRenderer;
import bookeditor.client.editor.render.TextLayoutRenderer;
import bookeditor.client.editor.text.StyleParams;
import bookeditor.client.editor.text.TextEditOps;
import bookeditor.client.editor.text.TextHitTester;
import bookeditor.client.util.ImageCache;
import bookeditor.data.BookData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import static net.minecraft.client.gui.screen.Screen.hasControlDown;

public class RichTextEditorWidget extends ClickableWidget {
    private static final int PAD_OUT = 8;
    private static final int PAD_IN = 8;
    private static final int LOGICAL_W = 960;
    private static final int LOGICAL_H = 600;

    private final TextRenderer textRenderer;
    private final java.util.function.Consumer<String> onImageUrlSeen;
    private final Runnable onDirty;

    private boolean editable;
    private BookData.Page page;

    private boolean bold;
    private boolean italic;
    private boolean underline;
    private int argb = 0xFF202020;
    private float size = 1.0f;

    private final CaretSelectionModel caret = new CaretSelectionModel();
    private final BrushTool brushTool = new BrushTool();
    private final ImageInteraction imageInteraction = new ImageInteraction();
    private final HistoryManager history = new HistoryManager();

    private final TextLayoutRenderer textLayoutRenderer = new TextLayoutRenderer();
    private final ImageRenderer imageRenderer = new ImageRenderer();
    private final CaretPainter caretPainter = new CaretPainter();
    private final TextEditOps textOps = new TextEditOps();

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

    public void setHeight(int height) { this.height = Math.max(40, height); clampScroll(); }
    public void setBounds(int x, int y, int width, int height) { this.setX(x); this.setY(y); this.setWidth(width); this.height = Math.max(40, height); clampScroll(); }

    private int innerLeft() { return getX() + PAD_OUT; }
    private int innerTop()  { return getY() + PAD_OUT; }
    private int innerW()    { return Math.max(0, getWidth() - PAD_OUT*2); }
    private int innerH()    { return Math.max(0, getHeight() - PAD_OUT*2); }
    private double scale() {
        double sw = (double)innerW() / (LOGICAL_W + PAD_IN*2.0);
        double sh = (double)innerH() / (LOGICAL_H + PAD_IN*2.0);
        return Math.max(0.1, Math.min(sw, sh));
    }
    private int canvasScreenLeft() { int scaledW = (int)Math.floor(scale() * (LOGICAL_W + PAD_IN*2)); return innerLeft() + Math.max(0, (innerW() - scaledW)/2); }
    private int canvasScreenTop()  { int scaledH = (int)Math.floor(scale() * (LOGICAL_H + PAD_IN*2)); return innerTop() + Math.max(0, (innerH() - scaledH)/2); }
    private int contentScreenLeft() { return canvasScreenLeft() + (int)Math.round(scale()*PAD_IN); }
    private int contentScreenTop()  { return canvasScreenTop()  + (int)Math.round(scale()*PAD_IN); }

    public void setEditable(boolean editable) { this.editable = editable; this.active = editable; }

    public void setContent(BookData.Page page) {
        this.page = page;
        normalize();
        caret.reset();
        if (page != null) caret.setCaret(Math.min(page.nodes.size() - 1, 0), 0);
        scrollY = 0;
        if (onImageUrlSeen != null && page != null) {
            for (BookData.Node n : page.nodes) {
                if (n instanceof BookData.ImageNode img && img.url != null && !img.url.isEmpty()) onImageUrlSeen.accept(img.url);
            }
        }
        history.clear();
        pushSnapshot();
        caretPainter.reset();
    }

    public void markSnapshot() { pushSnapshot(); }
    public boolean undo() {
        boolean applied = history.undo(this::applySnapshot);
        if (applied) caretPainter.reset();
        return applied;
    }
    public boolean redo() {
        boolean applied = history.redo(this::applySnapshot);
        if (applied) caretPainter.reset();
        return applied;
    }

    private void pushSnapshot() {
        if (page == null) return;
        history.pushSnapshot(page::toNbt);
    }
    private void pushSnapshotOnce() { if (page != null) history.pushSnapshotOnce(page::toNbt); }

    private void applySnapshot(net.minecraft.nbt.NbtCompound snap) {
        BookData.Page restored = BookData.Page.fromNbt(snap.copy());
        page.nodes.clear(); page.strokes.clear();
        page.bgArgb = restored.bgArgb;
        page.nodes.addAll(restored.nodes);
        page.strokes.addAll(restored.strokes);
        imageInteraction.clearSelection();
        caret.clearSelection();
        if (onImageUrlSeen != null) {
            for (BookData.Node n : page.nodes) {
                if (n instanceof BookData.ImageNode img && img.url != null && !img.url.isEmpty()) onImageUrlSeen.accept(img.url);
            }
        }
        clampScroll();
    }

    private void notifyDirty() {
        history.resetSnapshotArmed();
        if (onDirty != null) onDirty.run();
    }

    public void setBold(boolean bold) { this.bold = bold; }
    public boolean isBold() { return bold; }
    public void setItalic(boolean italic) { this.italic = italic; }
    public boolean isItalic() { return italic; }
    public void setUnderline(boolean underline) { this.underline = underline; }
    public boolean isUnderline() { return underline; }
    public void setColor(int argb) { this.argb = argb; brushTool.setBrushColor(argb); }
    public void setSize(float size) { this.size = size; }

    private StyleParams style() { return new StyleParams(bold, italic, underline, argb, size); }

    public boolean isBrushMode() { return brushTool.isBrushMode(); }
    public void setBrushMode(boolean brushMode) { brushTool.setBrushMode(brushMode); }
    public void setBrushColor(int argb) { brushTool.setBrushColor(argb); }
    public void setBrushSize(int px) { brushTool.setBrushSize(px); }

    public void setAlignment(int align) {
        if (page == null || page.nodes.isEmpty()) return;
        int idx = Math.max(0, Math.min(caret.getCaretNode(), page.nodes.size() - 1));
        BookData.Node n = page.nodes.get(idx);
        if (n instanceof BookData.TextNode tn) { pushSnapshotOnce(); tn.align = align; notifyDirty(); }
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
        caret.setCaret(page.nodes.size() - 1, 1);
        normalize();
        notifyDirty();
    }

    private void normalize() {
        bookeditor.client.editor.page.PageNormalizer.normalize(page, style());
        caret.ensureWithinPage(page);
    }

    @Override
    protected void renderButton(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int frame = isHovered() ? 0xFFAAAAAA : 0xFFBEBEBE;
        ctx.fill(getX() - 1, getY() - 1, getX() + getWidth() + 1, getY() + getHeight() + 1, frame);
        ctx.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xFFEFEFEF);

        int cLeft = canvasScreenLeft();
        int cTop = canvasScreenTop();
        int scaledW = (int)Math.floor(scale() * (LOGICAL_W + PAD_IN*2));
        int scaledH = (int)Math.floor(scale() * (LOGICAL_H + PAD_IN*2));
        int bg = page != null ? page.bgArgb : 0xFFF8F8F8;
        ctx.fill(cLeft, cTop, cLeft + scaledW, cTop + scaledH, bg);
        ctx.fill(cLeft - 1, cTop - 1, cLeft + scaledW + 1, cTop, 0x33000000);
        ctx.fill(cLeft - 1, cTop + scaledH, cLeft + scaledW + 1, cTop + scaledH + 1, 0x33000000);
        ctx.fill(cLeft - 1, cTop, cLeft, cTop + scaledH, 0x33000000);
        ctx.fill(cLeft + scaledW, cTop, cLeft + scaledW + 1, cTop + scaledH, 0x33000000);

        int scLeft = Math.max(innerLeft(), cLeft);
        int scTop = Math.max(innerTop(), cTop);
        int scRight = Math.min(innerLeft() + innerW(), cLeft + scaledW);
        int scBottom = Math.min(innerTop() + innerH(), cTop + scaledH);
        if (scRight > scLeft && scBottom > scTop) ctx.enableScissor(scLeft, scTop, scRight, scBottom);

        int startScreenX = contentScreenLeft();
        int startScreenY = contentScreenTop();
        double s = scale();

        brushTool.renderStrokes(ctx, page, startScreenX, startScreenY, s, scrollY);

        imageInteraction.beginFrame();

        TextLayoutRenderer.CaretPos caretPos = textLayoutRenderer.render(
                ctx, textRenderer, page, caret, startScreenX, startScreenY, scrollY, s, LOGICAL_W, textRenderer.fontHeight
        );

        imageRenderer.render(ctx, page, imageInteraction, startScreenX, startScreenY - (int)Math.round(s*scrollY), canvasScreenTop(), s, LOGICAL_W, LOGICAL_H);

        imageInteraction.renderSelectionHandles(ctx);

        caretPainter.renderCaret(ctx, this.isFocused(), editable, brushTool.isBrushMode(), caret.hasSelection(),
                caretPos.x, caretPos.y, startScreenX, startScreenY - (int)Math.round(s*scrollY), s, textRenderer.fontHeight);

        ctx.disableScissor();
    }

    private void clampScroll() { scrollY = 0; }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        clampScroll();
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isMouseOver(mouseX, mouseY)) return false;
        this.setFocused(true);

        int mx = (int)mouseX;
        int my = (int)mouseY;

        if (brushTool.beginStrokeIfNeeded(editable, page, mx, my, contentScreenLeft(), contentScreenTop(), scale(), scrollY)) {
            pushSnapshotOnce();
            return true;
        }

        imageInteraction.clearSelection();

        if (imageInteraction.mouseClicked(mx, my, editable, this::pushSnapshotOnce, page)) {
            return true;
        }

        int localX = (int)Math.floor(((mx - contentScreenLeft()) / scale()));
        int localY = (int)Math.floor(((my - contentScreenTop()) / scale())) + scrollY;
        int[] cNode = new int[1], cOff = new int[1];
        bookeditor.client.editor.text.TextHitTester.placeCaretByApprox(textRenderer, page, cNode, cOff, localX, localY, LOGICAL_W);
        caret.setCaret(cNode[0], cOff[0]);
        caret.clearSelection();
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (!editable) return false;

        int mx = (int)mouseX;
        int my = (int)mouseY;

        if (brushTool.continueStrokeIfActive(mx, my, contentScreenLeft(), contentScreenTop(), scale(), scrollY)) {
            return true;
        }

        if (imageInteraction.mouseDragged(mx, my, true, scale(), page)) {
            return true;
        }

        int localX = (int)Math.floor(((mx - contentScreenLeft()) / scale()));
        int localY = (int)Math.floor(((my - contentScreenTop()) / scale())) + scrollY;
        int[] cNode = new int[1], cOff = new int[1];
        TextHitTester.placeCaretByApprox(textRenderer, page, cNode, cOff, localX, localY, LOGICAL_W);
        if (!caret.hasSelection()) {
            caret.setAnchor(caret.getCaretNode(), caret.getCaretOffset());
        }
        caret.setCaret(cNode[0], cOff[0]);
        int[] st = caret.selectionStart();
        caret.setSelectionActive(!(cNode[0] == st[0] && cOff[0] == st[1]));
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean changed = false;
        if (imageInteraction.mouseReleased()) changed = true;
        if (brushTool.endStrokeIfActive(page)) changed = true;
        if (changed) notifyDirty();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!editable || page == null || !this.isFocused() || brushTool.isBrushMode()) return false;
        if (chr == '\r') chr = '\n';
        if (chr < 32 && chr != '\n' && chr != '\t') return false;

        pushSnapshotOnce();

        if (caret.hasSelection()) {
            textOps.deleteSelection(page, caret, style());
        }

        textOps.insertChar(page, caret, style(), chr);
        notifyDirty();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isFocused()) return false;

        if ((keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) && page != null) {
            if (imageInteraction.getSelectedImageIndex() >= 0) {
                pushSnapshotOnce();
                imageInteraction.deleteSelectedIfImage(page);
                notifyDirty();
                return true;
            }
        }

        if (!editable || page == null || brushTool.isBrushMode()) return false;

        boolean ctrl = hasControlDown();
        if (ctrl && (keyCode == GLFW.GLFW_KEY_A)) {
            caret.selectAll(page);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            pushSnapshotOnce();
            if (caret.hasSelection()) textOps.deleteSelection(page, caret, style());
            else textOps.backspace(page, caret, style());
            caret.clearSelection();
            notifyDirty();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
            pushSnapshotOnce();
            if (caret.hasSelection()) textOps.deleteSelection(page, caret, style());
            else textOps.deleteForward(page, caret, style());
            caret.clearSelection();
            notifyDirty();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            pushSnapshotOnce();
            if (caret.hasSelection()) textOps.deleteSelection(page, caret, style());
            boolean r = charTyped('\n', modifiers);
            if (r) { caret.clearSelection(); notifyDirty(); }
            return r;
        } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
                if (!caret.hasSelection()) { caret.setAnchor(caret.getCaretNode(), caret.getCaretOffset()); }
                caret.moveLeft(page);
                caret.setSelectionActive(true);
            } else {
                caret.moveLeft(page);
                caret.clearSelection();
            }
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
                if (!caret.hasSelection()) { caret.setAnchor(caret.getCaretNode(), caret.getCaretOffset()); }
                caret.moveRight(page);
                caret.setSelectionActive(true);
            } else {
                caret.moveRight(page);
                caret.clearSelection();
            }
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            mouseScrolled(0,0,1.0);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            mouseScrolled(0,0,-1.0);
            return true;
        }
        return false;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}