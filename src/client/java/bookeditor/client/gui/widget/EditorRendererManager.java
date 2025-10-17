package bookeditor.client.gui.widget;

import net.minecraft.client.gui.DrawContext;

public class EditorRendererManager {
    private final EditorState state;

    public EditorRendererManager(EditorState state, EditorToolManager toolManager, EditorStyleManager styleManager) {
        this.state = state;
    }
    public void renderButton(DrawContext ctx, int mouseX, int mouseY, float delta) {
        RichTextEditorWidget widget = state.getWidget();
        renderFrame(ctx, widget);
        renderCanvas(ctx);
        enableScissor(ctx);
        if (state.textBoxCreationTool.isActive()) {
            state.textBoxCreationTool.updatePreview(mouseX, mouseY, state.contentScreenLeft(), state.contentScreenTop(), state.scale(), state.scrollY);
        }
        state.drawingTool.renderStrokes(ctx, state.page, state.contentScreenLeft(), state.contentScreenTop(), state.scale(), state.scrollY);
        state.editorRenderer.render(ctx, state.page, state.mode, state.imageInteraction, state.textBoxInteraction, state.textBoxCaret,
                state.textRenderer, widget.isFocused(), state.editable, state.contentScreenLeft(), state.contentScreenTop(),
                state.canvasScreenTop(), state.scale(), state.scrollY, EditorState.LOGICAL_W, EditorState.LOGICAL_H);
        if (state.textBoxCreationTool.isActive()) {
            state.textBoxCreationTool.renderPreview(ctx, state.contentScreenLeft(), state.contentScreenTop(), state.scale(), state.scrollY);
        }
        if (state.eraserTool.isActive()) {
            state.eraserTool.renderPreview(ctx, mouseX, mouseY, state.contentScreenLeft(), state.contentScreenTop(), state.scale(), state.scrollY);
        }
        ctx.disableScissor();
    }
    private void renderFrame(DrawContext ctx, RichTextEditorWidget widget) {
        int frame = widget.isHovered() ? 0xFFAAAAAA : 0xFFBEBEBE;
        ctx.fill(widget.getX() - 1, widget.getY() - 1, widget.getX() + widget.getWidth() + 1, widget.getY() + widget.getHeight() + 1, frame);
        ctx.fill(widget.getX(), widget.getY(), widget.getX() + widget.getWidth(), widget.getY() + widget.getHeight(), 0xFFEFEFEF);
    }
    private void renderCanvas(DrawContext ctx) {
        int cLeft = state.canvasScreenLeft();
        int cTop = state.canvasScreenTop();
        int scaledW = (int) Math.floor(state.scale() * (EditorState.LOGICAL_W + EditorState.PAD_IN * 2));
        int scaledH = (int) Math.floor(state.scale() * (EditorState.LOGICAL_H + EditorState.PAD_IN * 2));
        int bg = state.page != null ? state.page.bgArgb : 0xFFF8F8F8;
        ctx.fill(cLeft, cTop, cLeft + scaledW, cTop + scaledH, bg);
        ctx.fill(cLeft - 1, cTop - 1, cLeft + scaledW + 1, cTop, 0x33000000);
        ctx.fill(cLeft - 1, cTop + scaledH, cLeft + scaledW + 1, cTop + scaledH + 1, 0x33000000);
        ctx.fill(cLeft - 1, cTop, cLeft, cTop + scaledH, 0x33000000);
        ctx.fill(cLeft + scaledW, cTop, cLeft + scaledW + 1, cTop + scaledH, 0x33000000);
    }
    private void enableScissor(DrawContext ctx) {
        int cLeft = state.canvasScreenLeft();
        int cTop = state.canvasScreenTop();
        int scaledW = (int) Math.floor(state.scale() * (EditorState.LOGICAL_W + EditorState.PAD_IN * 2));
        int scaledH = (int) Math.floor(state.scale() * (EditorState.LOGICAL_H + EditorState.PAD_IN * 2));
        int scLeft = Math.max(state.innerLeft(), cLeft);
        int scTop = Math.max(state.innerTop(), cTop);
        int scRight = Math.min(state.innerLeft() + state.innerW(), cLeft + scaledW);
        int scBottom = Math.min(state.innerTop() + state.innerH(), cTop + scaledH);
        if (scRight > scLeft && scBottom > scTop) ctx.enableScissor(scLeft, scTop, scRight, scBottom);
    }
}