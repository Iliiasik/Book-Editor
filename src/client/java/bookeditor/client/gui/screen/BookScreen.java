package bookeditor.client.gui.screen;

import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.components.AdaptiveToolbar;
import bookeditor.client.gui.components.BookNavigator;
import bookeditor.client.gui.render.AuthorBadgeRenderer;
import bookeditor.client.gui.widget.ColorPickerDropdown;
import bookeditor.client.gui.widget.CustomTextField;
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
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class BookScreen extends Screen implements WidgetHost {

    private static final int MARGIN = 10;
    private static final int GAP = 5;
    private static final int BTN_H = 18;

    private final Hand hand;
    private final net.minecraft.item.ItemStack stack;
    private BookData data;

    private int bookPage = 0;
    private CustomTextField titleField;
    private RichTextEditorWidget editor;
    private BookNavigator bookNavigator;
    private AdaptiveToolbar toolbar;

    public BookScreen(net.minecraft.item.ItemStack stack, Hand hand) {
        super(Text.translatable("screen.bookeditor.title"));
        this.stack = stack;
        this.hand = hand;
        this.data = BookData.readFrom(stack);
        if (this.data.pages.isEmpty()) {
            BookData.Page p = new BookData.Page();
            p.nodes.add(new BookData.TextNode("", false, false, false, 0xFF202020, 1.0f, BookData.ALIGN_LEFT));
            this.data.pages.add(p);
        }
    }

    @Override
    protected void init() {
        clearChildren();

        int y = MARGIN;

        if (!data.signed) {
            int titleW = Math.min(220, this.width / 4);
            titleField = new CustomTextField(textRenderer, MARGIN, y, titleW, BTN_H,
                    Text.translatable("screen.bookeditor.book_title"));
            titleField.setText(data.title);
            addDrawableChild(titleField);
        }

        bookNavigator = new BookNavigator(
                this,
                this.width,
                y,
                BTN_H,
                () -> bookPage,
                () -> data.pages.size(),
                this::setPage
        );
        bookNavigator.build();

        y += BTN_H + GAP;

        int toolbarX = MARGIN;
        int toolbarY = y;
        int toolbarWidth = this.width - MARGIN * 2;

        y += BTN_H + GAP;

        int editorX = MARGIN;
        int editorY = y;
        int editorWidth = this.width - MARGIN * 2;
        int editorHeight = Math.max(160, this.height - editorY - MARGIN);

        editor = new RichTextEditorWidget(
                textRenderer, editorX, editorY, editorWidth, editorHeight,
                !data.signed, ImageCache::requestTexture, this::onDirty
        );
        addDrawableChild(editor);

        toolbar = new AdaptiveToolbar(
                this, editor, this::onDirty,
                () -> data.pages.get(bookPage).bgArgb,
                this::setPageBgColor,
                this::openInsertDialog,
                this::createNewPage,
                this::deleteCurrentPage,
                this::signBook,
                toolbarX, toolbarY, BTN_H, GAP, toolbarWidth
        );
        toolbar.build();

        editor.setContent(data.pages.get(bookPage));

        updateUI();
    }

    private void updateUI() {
        if (bookNavigator != null) bookNavigator.updateState();
        if (toolbar != null) toolbar.setVisible(!data.signed);
        if (editor != null) editor.setEditable(!data.signed);
        if (titleField != null) titleField.visible = !data.signed;
        prefetchPageImages();
    }

    private void setPage(int page) {
        if (page < 0 || page >= data.pages.size()) return;
        bookPage = page;
        editor.setContent(data.pages.get(bookPage));
        toolbar.updateCanvasColor(data.pages.get(bookPage).bgArgb);
        bookNavigator.updateState();
        prefetchPageImages();
    }

    private void setPageBgColor(int color) {
        if (bookPage >= 0 && bookPage < data.pages.size()) {
            data.pages.get(bookPage).bgArgb = color;
            onDirty();
        }
    }

    private void prefetchPageImages() {
        var p = data.pages.get(bookPage);
        for (var n : p.nodes) {
            if (n instanceof BookData.ImageNode img && img.url != null && !img.url.isEmpty()) {
                ImageCache.requestTexture(img.url);
            }
        }
    }

    private void openInsertDialog() {
        if (data.signed) return;
        MinecraftClient.getInstance().setScreen(new ImageInsertScreen(this, (url, w, h, isGifIgnored) -> {
            editor.markSnapshot();
            editor.insertImage(url, w, h, false);
            onDirty();
        }, false));
    }

    private void createNewPage() {
        if (data.signed) return;
        BookData.Page p = new BookData.Page();
        var cur = data.pages.get(bookPage);
        p.bgArgb = cur.bgArgb;
        p.nodes.add(new BookData.TextNode("", false, false, false, 0xFF202020, 1.0f, BookData.ALIGN_LEFT));
        data.pages.add(bookPage + 1, p);
        setPage(bookPage + 1);
        onDirty();
    }

    private void deleteCurrentPage() {
        if (data.signed || data.pages.size() <= 1) return;
        data.pages.remove(bookPage);
        int newIndex = Math.max(0, Math.min(bookPage, data.pages.size() - 1));
        setPage(newIndex);
        onDirty();
    }

    private void signBook() {
        if (data.signed) return;
        if (titleField != null) data.title = titleField.getText();
        var player = MinecraftClient.getInstance().player;
        if (player != null) {
            data.authorName = player.getGameProfile().getName();
            data.authorUuid = player.getUuid();
            data.signed = true;
            updateUI();
            onDirty();
        }
    }

    private void onDirty() {
        if (!data.signed && titleField != null) {
            data.title = titleField.getText();
        }
        BookSyncService.sendUpdate(hand, data);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx);
        super.render(ctx, mouseX, mouseY, delta);

        if (data.signed) {
            AuthorBadgeRenderer.renderBadge(ctx, textRenderer, this.width, MARGIN + 10, BTN_H, data);
        }

        if (bookNavigator != null) {
            bookNavigator.renderPageCounter(ctx, textRenderer, data.pages.size());
        }

        for (var child : this.children()) {
            if (child instanceof ColorPickerDropdown dropdown && dropdown.isExpanded()) {
                dropdown.renderDropdown(ctx, mouseX, mouseY);
            }
        }

        if (toolbar != null && editor != null) {
            toolbar.syncWithEditor();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (var child : this.children()) {
            if (child instanceof ColorPickerDropdown dropdown && dropdown.isExpanded()) {
                if (dropdown.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
    }

    @Override
    public void close() {
        onDirty();
        super.close();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!data.signed && hasControlDown()) {
            if (keyCode == GLFW.GLFW_KEY_B) {
                editor.setBold(!editor.isBold());
                editor.applyStyleToSelection();
                toolbar.refreshFormatButtons();
                onDirty();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_I) {
                editor.setItalic(!editor.isItalic());
                editor.applyStyleToSelection();
                toolbar.refreshFormatButtons();
                onDirty();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_U) {
                editor.setUnderline(!editor.isUnderline());
                editor.applyStyleToSelection();
                toolbar.refreshFormatButtons();
                onDirty();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_Z) {
                if (editor.undo()) onDirty();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_Y) {
                if (editor.redo()) onDirty();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_C) {
                editor.copySelection();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_V) {
                editor.paste();
                onDirty();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_X) {
                editor.cutSelection();
                onDirty();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_A) {
                editor.selectAll();
                return true;
            }
        }
        if (!data.signed && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && hasControlDown()) {
            signBook();
            return true;
        }
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