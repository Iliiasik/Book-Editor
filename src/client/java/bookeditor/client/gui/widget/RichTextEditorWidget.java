package bookeditor.client.gui.widget;

import bookeditor.client.editor.tools.DrawingTool;
import bookeditor.data.BookData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class RichTextEditorWidget extends ClickableWidget {
    private int widgetHeight;
    private final EditorState state;
    private final EditorHistoryManager historyManager;
    private final EditorStyleManager styleManager;
    private final EditorToolManager toolManager;
    private final EditorRendererManager rendererManager;

    public RichTextEditorWidget(TextRenderer textRenderer, int x, int y, int width, int height,
                                boolean editable, java.util.function.Consumer<String> onImageUrlSeen, Runnable onDirty) {
        super(x, y, width, height, Text.literal("RichEditor"));
        this.widgetHeight = height;
        this.state = new EditorState(textRenderer, editable, onImageUrlSeen, onDirty, this);
        this.historyManager = new EditorHistoryManager(state);
        this.styleManager = new EditorStyleManager(state, historyManager);
        this.toolManager = new EditorToolManager(state, historyManager, styleManager);
        this.rendererManager = new EditorRendererManager(state, toolManager, styleManager);
        this.active = editable;
    }

    public void setWidgetHeight(int height) {
        this.widgetHeight = height;
    }

    @Override
    public int getHeight() {
        return widgetHeight;
    }

    public boolean superMouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public void setHeight(int height) {
        state.setHeight(height);
    }
    public void setEditable(boolean editable) { state.setEditable(editable); }
    public void setContent(BookData.Page page) { state.setContent(page); historyManager.onContentSet(); }
    public void markSnapshot() { historyManager.pushSnapshot(); }
    public boolean undo() { return historyManager.undo(); }
    public boolean redo() { return historyManager.redo(); }
    public void deactivateAllTools() { toolManager.deactivateAllTools(); }
    public void setBold(boolean bold) { styleManager.setBold(bold); }
    public void setItalic(boolean italic) { styleManager.setItalic(italic); }
    public void setUnderline(boolean underline) { styleManager.setUnderline(underline); }
    public void setColor(int argb) { styleManager.setColor(argb); }
    public void setSize(float size) { styleManager.setSize(size); }
    public void setTextBoxBgColor(int argb) { styleManager.setTextBoxBgColor(argb); }
    public void setDrawingToolColor(int argb) { toolManager.setDrawingToolColor(argb); }
    public void setDrawingTool(DrawingTool tool) { toolManager.setDrawingTool(tool); }
    public DrawingTool getCurrentDrawingTool() { return toolManager.getCurrentDrawingTool(); }
    public void setToolSize(int size) { toolManager.setToolSize(size); }
    public void activateTextBoxTool() { toolManager.activateTextBoxTool(); }
    public void setAlignment(int align) { styleManager.setAlignment(align); }
    public void insertImage(String url, int w, int h, boolean gif) { toolManager.insertImage(url, w, h, gif); }
    public void applyStyleToSelection() { styleManager.applyStyleToSelection(); }
    public void copySelection() { styleManager.copySelection(); }
    public void cutSelection() { styleManager.cutSelection(); }
    public void paste() { styleManager.paste(); }
    public void selectAll() { styleManager.selectAll(); }
    public void syncStylesFromSelection() { styleManager.syncStylesFromSelection(); }
    public int getColor() { return styleManager.getColor(); }
    public boolean isBold() { return styleManager.isBold(); }
    public boolean isItalic() { return styleManager.isItalic(); }
    public boolean isUnderline() { return styleManager.isUnderline(); }
    public float getSize() { return styleManager.getSize(); }

    @Override
    protected void renderButton(DrawContext ctx, int mouseX, int mouseY, float delta) {
        rendererManager.renderButton(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return toolManager.mouseScrolled(mouseX, mouseY, amount);
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return toolManager.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        return toolManager.mouseDragged(mouseX, mouseY, button, dx, dy);
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return toolManager.mouseReleased(mouseX, mouseY, button);
    }
    @Override
    public boolean charTyped(char chr, int modifiers) {
        return toolManager.charTyped(chr, modifiers);
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return toolManager.keyPressed(keyCode, scanCode, modifiers);
    }
    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}