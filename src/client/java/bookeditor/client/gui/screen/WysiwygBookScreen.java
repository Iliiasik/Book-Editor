package bookeditor.client.gui.screen;

import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.components.CanvasToolbar;
import bookeditor.client.gui.components.FormattingToolbar;
import bookeditor.client.gui.components.NavigationBar;
import bookeditor.client.gui.render.AuthorBadgeRenderer;
import bookeditor.client.gui.widget.RichTextEditorWidget;
import bookeditor.client.net.BookSyncService;
import bookeditor.client.util.ImageCache;
import bookeditor.data.BookData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.Objects;

public class WysiwygBookScreen extends Screen implements WidgetHost {

    private static final int MARGIN = 10;
    private static final int GAP = 5;
    private static final int BTN_H = 18;

    private final Hand hand;
    private final net.minecraft.item.ItemStack stack;
    private BookData data;

    private TextFieldWidget titleField;

    private int toolsPage = 0;
    private final int toolsPages = 2;
    private ButtonWidget toolsPrevBtn, toolsNextBtn;

    private RichTextEditorWidget editor;
    private int page = 0;

    private int toolbarY = 0;

    private NavigationBar navigationBar;
    private FormattingToolbar formattingToolbar;
    private CanvasToolbar canvasToolbar;

    public WysiwygBookScreen(net.minecraft.item.ItemStack stack, Hand hand) {
        super(Text.translatable("screen.bookeditor.title"));
        this.stack = stack;
        this.hand = hand;
        this.data = BookData.readFrom(stack);
        if (this.data.pages.isEmpty()) {
            BookData.Page p = new BookData.Page();
            p.nodes.add(new BookData.TextNode("", false,false,false, 0xFF202020, 1.0f, BookData.ALIGN_LEFT));
            this.data.pages.add(p);
        }
    }

    @Override
    protected void init() {
        clearChildren();

        int y = MARGIN;

        if (!data.signed) {
            int titleW = Math.max(220, this.width - MARGIN*2 - 160);
            titleField = new TextFieldWidget(textRenderer, MARGIN, y, titleW, BTN_H, Text.translatable("screen.bookeditor.book_title"));
            titleField.setText(data.title);
            addDrawableChild(titleField);
        } else {
            titleField = null;
        }

        int navBlockW = 18 + GAP + 36 + GAP + 28 + GAP + 18;
        int navX = this.width - MARGIN - navBlockW;
        int navY = y;

        navigationBar = new NavigationBar(
                this,
                navX,
                navY,
                BTN_H,
                () -> page,
                () -> data.pages.size(),
                p -> setPage(p, true),
                () -> changePage(-1),
                () -> changePage(1)
        );
        navigationBar.build();

        y += BTN_H + 4;

        int x = MARGIN;
        toolsPrevBtn = addDrawableChild(ButtonWidget.builder(Text.literal("◀"), b -> {
            toolsPage = (toolsPage - 1 + toolsPages) % toolsPages;
            updateToolsVisibility();
        }).dimensions(x, y, 18, BTN_H).build());
        x += 18 + GAP;
        toolsNextBtn = addDrawableChild(ButtonWidget.builder(Text.literal("▶"), b -> {
            toolsPage = (toolsPage + 1) % toolsPages;
            updateToolsVisibility();
        }).dimensions(x, y, 18, BTN_H).build());

        int rowY = y;
        toolbarY = rowY;

        int editorY = rowY + BTN_H + 6;
        int editorX = MARGIN;
        int editorW = this.width - MARGIN * 2;
        int editorH = Math.max(160, this.height - editorY - MARGIN);
        editor = new RichTextEditorWidget(
                textRenderer, editorX, editorY, editorW, editorH,
                !data.signed,
                ImageCache::requestTexture,
                this::onDirty
        );

        int toolsStartX = MARGIN + 18 + GAP + 18 + GAP;
        formattingToolbar = new FormattingToolbar(
                this, editor, this::onDirty,
                toolsStartX, rowY, BTN_H, GAP
        );
        formattingToolbar.build();

        canvasToolbar = new CanvasToolbar(
                this,
                editor,
                this::openInsert,
                this::createNewPage,
                this::deleteCurrentPage,
                this::sign,
                this::onDirty,
                argb -> { data.pages.get(page).bgArgb = argb; },
                toolsStartX, rowY, BTN_H, GAP
        );
        canvasToolbar.build(data.pages.get(page).bgArgb);

        editor.setContent(data.pages.get(page));
        formattingToolbar.setInitialTextColor(0xFF202020);
        canvasToolbar.setCanvasColor(data.pages.get(page).bgArgb);
        canvasToolbar.setBrushSize(3);
        addDrawableChild(editor);

        prefetchPageImages();

        applySignedVisibility();
        updateToolsVisibility();
    }

    private void changePage(int dir) { setPage(this.page + dir, true); }

    private void setPage(int idx, boolean clamp) {
        if (clamp) { if (idx < 0) idx = 0; if (idx >= data.pages.size()) idx = data.pages.size() - 1; }
        else { if (idx < 0) idx = 0; if (idx > data.pages.size()) idx = data.pages.size(); }
        this.page = idx;
        editor.setContent(data.pages.get(page));
        canvasToolbar.setCanvasColor(data.pages.get(page).bgArgb);
        navigationBar.updateFieldFromState();
        prefetchPageImages();
    }

    private void prefetchPageImages() {
        var p = data.pages.get(page);
        for (var n : p.nodes) {
            if (n instanceof BookData.ImageNode img && img.url != null && !img.url.isEmpty()) {
                ImageCache.requestTexture(img.url);
            }
        }
    }

    private void openInsert() {
        if (data.signed) return;
        MinecraftClient.getInstance().setScreen(new ImageInsertScreen(this, (url, w, h, isGifIgnored) -> {
            editor.markSnapshot();
            editor.insertImage(url, w, h, false);
            onDirty();
        }, false));
    }

    private void createNewPage() {
        if (!data.signed) {
            onDirty();
            BookData.Page p = new BookData.Page();
            var cur = data.pages.get(page);
            p.bgArgb = cur.bgArgb;
            p.nodes.add(new BookData.TextNode("", false,false,false, 0xFF202020, 1.0f, BookData.ALIGN_LEFT));
            data.pages.add(page + 1, p);
            setPage(page + 1, false);
            onDirty();
        }
    }

    private void deleteCurrentPage() {
        if (!data.signed) {
            if (data.pages.size() > 1) {
                data.pages.remove(page);
                int newIndex = Math.max(0, Math.min(page, data.pages.size() - 1));
                setPage(newIndex, false);
            } else {
                BookData.Page p = data.pages.get(0);
                p.nodes.clear(); p.strokes.clear();
                p.nodes.add(new BookData.TextNode("", false,false,false, 0xFF202020, 1.0f, BookData.ALIGN_LEFT));
                editor.setContent(p);
            }
            onDirty();
        }
    }

    private void onDirty() {
        if (titleField != null) data.title = titleField.getText();
        BookSyncService.sendUpdate(hand, data);
    }

    private void applySignedVisibility() {
        boolean toolsVisible = !data.signed;

        if (titleField != null) titleField.visible = toolsVisible;

        if (navigationBar != null) navigationBar.setVisible(true);

        updateToolsVisibility();

        if (editor != null) editor.setEditable(!data.signed);
    }

    private void updateToolsVisibility() {
        boolean toolsVisible = !data.signed;

        boolean p0 = toolsPage == 0 && toolsVisible;
        boolean p1 = toolsPage == 1 && toolsVisible;

        if (formattingToolbar != null) formattingToolbar.setVisible(p0);
        if (canvasToolbar != null) canvasToolbar.setVisible(p1, data.signed);

        if (toolsPrevBtn != null) toolsPrevBtn.visible = toolsVisible;
        if (toolsNextBtn != null) toolsNextBtn.visible = toolsVisible;
    }

    private void sign() {
        if (data.signed) return;
        if (titleField != null) data.title = titleField.getText();
        var p = java.util.Objects.requireNonNull(net.minecraft.client.MinecraftClient.getInstance().player);
        data.authorName = p.getGameProfile().getName();
        data.authorUuid = p.getUuid();
        data.signed = true;
        applySignedVisibility();
        onDirty();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx);
        super.render(ctx, mouseX, mouseY, delta);
        if (data.signed) {
            AuthorBadgeRenderer.renderBadge(ctx, textRenderer, this.width, toolbarY, BTN_H, data);
        }
    }

    @Override
    public void resize(MinecraftClient mc, int width, int height) {
        super.resize(mc, width, height);
        this.init();
    }

    @Override
    public void close() {
        onDirty();
        super.close();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!data.signed && hasControlDown()) {
            if (keyCode == 66) { editor.setBold(!editor.isBold()); if (formattingToolbar != null) formattingToolbar.refreshFormatButtons(); onDirty(); return true; }
            else if (keyCode == 73) { editor.setItalic(!editor.isItalic()); if (formattingToolbar != null) formattingToolbar.refreshFormatButtons(); onDirty(); return true; }
            else if (keyCode == 85) { editor.setUnderline(!editor.isUnderline()); if (formattingToolbar != null) formattingToolbar.refreshFormatButtons(); onDirty(); return true; }
            else if (keyCode == 90) { if (editor.undo()) onDirty(); return true; }
            else if (keyCode == 89) { if (editor.redo()) onDirty(); return true; }
        }
        if (!data.signed && (keyCode == 257 || keyCode == 335) && hasControlDown()) { sign(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public <T extends Element & Drawable & Selectable> T addDrawable(T widget) {
        return addDrawableChild(widget);
    }

    @Override
    public TextRenderer getTextRenderer() {
        return this.textRenderer;
    }
}