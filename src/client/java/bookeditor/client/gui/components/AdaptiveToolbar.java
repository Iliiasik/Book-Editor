package bookeditor.client.gui.components;

import bookeditor.client.editor.tools.DrawingTool;
import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.widget.ColorPickerDropdown;
import bookeditor.client.gui.widget.CustomButton;
import bookeditor.client.gui.widget.IconButton;
import bookeditor.client.gui.widget.NumericTextField;
import bookeditor.client.gui.widget.RichTextEditorWidget;
import bookeditor.client.util.IconUtils;
import bookeditor.data.BookData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.function.Consumer;

public class AdaptiveToolbar {
    private final WidgetHost host;
    private final RichTextEditorWidget editor;
    private final Runnable onDirty;
    private final IntSupplier getCanvasColor;
    private final Consumer<Integer> setCanvasColor;
    private final Runnable openInsertDialog;
    private final Runnable createNewPage;
    private final Runnable deleteCurrentPage;
    private final Runnable signAction;
    private final int x;
    private final int y;
    private final int btnH;
    private final int gap;
    private final int availableWidth;

    private CustomButton prevPageBtn;
    private CustomButton nextPageBtn;

    private final List<ToolbarSection> sections = new ArrayList<>();
    private final List<List<ToolbarWidget>> pages = new ArrayList<>();
    private int currentPage = 0;

    private IconButton boldBtn;
    private IconButton italicBtn;
    private IconButton underlineBtn;
    private NumericTextField sizeField;
    private ColorPickerDropdown textColorBtn;
    private ColorPickerDropdown canvasColorBtn;

    private final Map<DrawingTool, IconButton> toolButtonMap = new HashMap<>();

    public AdaptiveToolbar(WidgetHost host, RichTextEditorWidget editor, Runnable onDirty,
                           IntSupplier getCanvasColor, Consumer<Integer> setCanvasColor,
                           Runnable openInsertDialog, Runnable createNewPage,
                           Runnable deleteCurrentPage, Runnable signAction,
                           int x, int y, int btnH, int gap, int availableWidth) {
        this.host = host;
        this.editor = editor;
        this.onDirty = onDirty;
        this.getCanvasColor = getCanvasColor;
        this.setCanvasColor = setCanvasColor;
        this.openInsertDialog = openInsertDialog;
        this.createNewPage = createNewPage;
        this.deleteCurrentPage = deleteCurrentPage;
        this.signAction = signAction;
        this.x = x;
        this.y = y;
        this.btnH = btnH;
        this.gap = gap;
        this.availableWidth = availableWidth;
    }

    public void build() {
        createNavigationButtons();
        createSections();
        distributeToPages();
        showPage(0);
    }

    private void createNavigationButtons() {
        prevPageBtn = new CustomButton(x, y, 20, btnH, Text.literal("◀"), b -> {
            if (currentPage > 0) {
                showPage(currentPage - 1);
            }
        });
        prevPageBtn.visible = false;
        host.addDrawable(prevPageBtn);

        nextPageBtn = new CustomButton(x + 24, y, 20, btnH, Text.literal("▶"), b -> {
            if (currentPage < pages.size() - 1) {
                showPage(currentPage + 1);
            }
        });
        nextPageBtn.visible = false;
        host.addDrawable(nextPageBtn);
    }

    private void createSections() {
        createHistorySection();
        createTextStyleSection();
        createTextSizeSection();
        createAlignmentSection();
        createContentSection();
        createDrawingToolsSection();
        createCanvasSection();
        createPageManagementSection();
    }

    private void createHistorySection() {
        ToolbarSection section = new ToolbarSection("History");

        section.addWidget(createIconButton(IconUtils.ICON_UNDO, "tooltip.bookeditor.undo", b -> {
            if (editor.undo()) onDirty.run();
        }), 18);

        section.addWidget(createIconButton(IconUtils.ICON_REDO, "tooltip.bookeditor.redo", b -> {
            if (editor.redo()) onDirty.run();
        }), 18);

        sections.add(section);
    }

    private void createTextStyleSection() {
        ToolbarSection section = new ToolbarSection("Text Style");

        boldBtn = createIconButton(IconUtils.ICON_BOLD, "tooltip.bookeditor.bold", b -> {
            editor.setBold(!editor.isBold());
            editor.applyStyleToSelection();
            refreshFormatButtons();
            onDirty.run();
        });
        section.addWidget(boldBtn, 18);

        italicBtn = createIconButton(IconUtils.ICON_ITALIC, "tooltip.bookeditor.italic", b -> {
            editor.setItalic(!editor.isItalic());
            editor.applyStyleToSelection();
            refreshFormatButtons();
            onDirty.run();
        });
        section.addWidget(italicBtn, 18);

        underlineBtn = createIconButton(IconUtils.ICON_UNDERLINE, "tooltip.bookeditor.underline", b -> {
            editor.setUnderline(!editor.isUnderline());
            editor.applyStyleToSelection();
            refreshFormatButtons();
            onDirty.run();
        });
        section.addWidget(underlineBtn, 18);

        textColorBtn = new ColorPickerDropdown(0, 0, argb -> {
            editor.setColor(argb);
            editor.applyStyleToSelection();
            onDirty.run();
        }, 0xFF202020);
        textColorBtn.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.translatable("tooltip.bookeditor.text_color")));
        host.addDrawable(textColorBtn);
        section.addWidget(textColorBtn, 20);

        sections.add(section);
    }

    private void createTextSizeSection() {
        ToolbarSection section = new ToolbarSection("Size");

        IconButton decreaseBtn = createIconButton(IconUtils.ICON_DECREASE_SIZE, "tooltip.bookeditor.decrease_size", b -> {
            float currentSize = editor.getSize();
            float newSize = Math.max(0.5f, currentSize - 0.1f);
            editor.setSize(newSize);
            editor.applyStyleToSelection();
            if (sizeField != null) sizeField.setText(String.format("%.1f", newSize));
            onDirty.run();
        });
        section.addWidget(decreaseBtn, 18);

        sizeField = new NumericTextField(host.getTextRenderer(), 0, 0, 40, btnH,
                Text.translatable("tooltip.bookeditor.text_size"));
        sizeField.setText("1.0");
        sizeField.setOnEnterPressed(() -> {
            try {
                float s = Float.parseFloat(sizeField.getText().trim());
                s = Math.max(0.5f, Math.min(3.0f, s));
                editor.setSize(s);
                editor.applyStyleToSelection();
                sizeField.setText(String.format("%.1f", s));
                onDirty.run();
            } catch (NumberFormatException ignored) {}
        });
        host.addDrawable(sizeField);
        section.addWidget(sizeField, 40);

        IconButton increaseBtn = createIconButton(IconUtils.ICON_INCREASE_SIZE, "tooltip.bookeditor.increase_size", b -> {
            float currentSize = editor.getSize();
            float newSize = Math.min(3.0f, currentSize + 0.1f);
            editor.setSize(newSize);
            editor.applyStyleToSelection();
            if (sizeField != null) sizeField.setText(String.format("%.1f", newSize));
            onDirty.run();
        });
        section.addWidget(increaseBtn, 18);

        sections.add(section);
    }

    private void createAlignmentSection() {
        ToolbarSection section = new ToolbarSection("Align");

        section.addWidget(createIconButton(IconUtils.ICON_ALIGN_LEFT, "tooltip.bookeditor.align_left", b -> {
            editor.setAlignment(BookData.ALIGN_LEFT);
            onDirty.run();
        }), 18);

        section.addWidget(createIconButton(IconUtils.ICON_ALIGN_CENTER, "tooltip.bookeditor.align_center", b -> {
            editor.setAlignment(BookData.ALIGN_CENTER);
            onDirty.run();
        }), 18);

        section.addWidget(createIconButton(IconUtils.ICON_ALIGN_RIGHT, "tooltip.bookeditor.align_right", b -> {
            editor.setAlignment(BookData.ALIGN_RIGHT);
            onDirty.run();
        }), 18);

        sections.add(section);
    }

    private void createContentSection() {
        ToolbarSection section = new ToolbarSection("Content");

        section.addWidget(createIconButton(IconUtils.ICON_TEXTBOX, "tooltip.bookeditor.textbox", b -> {
            editor.activateTextBoxTool();
            onDirty.run();
        }), 18);

        section.addWidget(createIconButton(IconUtils.ICON_IMAGE, "tooltip.bookeditor.image", b -> openInsertDialog.run()), 18);

        ColorPickerDropdown textBoxBgBtn = new ColorPickerDropdown(0, 0, argb -> {
            editor.setTextBoxBgColor(argb);
            onDirty.run();
        }, 0x00FFFFFF);
        textBoxBgBtn.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.translatable("tooltip.bookeditor.textbox_bg_color")));
        host.addDrawable(textBoxBgBtn);
        section.addWidget(textBoxBgBtn, 20);

        sections.add(section);
    }

    private void createDrawingToolsSection() {
        ToolbarSection section = new ToolbarSection("Drawing");

        IconButton brushBtn = createIconButton(IconUtils.ICON_BRUSH, "tooltip.bookeditor.brush", b ->
                toggleDrawingTool(DrawingTool.BRUSH));
        toolButtonMap.put(DrawingTool.BRUSH, brushBtn);
        section.addWidget(brushBtn, 18);

        IconButton sprayBtn = createIconButton(IconUtils.ICON_SPRAY, "tooltip.bookeditor.spray", b ->
                toggleDrawingTool(DrawingTool.SPRAY));
        toolButtonMap.put(DrawingTool.SPRAY, sprayBtn);
        section.addWidget(sprayBtn, 18);

        IconButton lineBtn = createIconButton(IconUtils.ICON_LINE, "tooltip.bookeditor.line", b ->
                toggleDrawingTool(DrawingTool.LINE));
        toolButtonMap.put(DrawingTool.LINE, lineBtn);
        section.addWidget(lineBtn, 18);

        IconButton rectangleBtn = createIconButton(IconUtils.ICON_RECTANGLE, "tooltip.bookeditor.rectangle", b ->
                toggleDrawingTool(DrawingTool.RECTANGLE));
        toolButtonMap.put(DrawingTool.RECTANGLE, rectangleBtn);
        section.addWidget(rectangleBtn, 18);

        IconButton circleBtn = createIconButton(IconUtils.ICON_CIRCLE, "tooltip.bookeditor.circle", b ->
                toggleDrawingTool(DrawingTool.CIRCLE));
        toolButtonMap.put(DrawingTool.CIRCLE, circleBtn);
        section.addWidget(circleBtn, 18);

        IconButton eraserBtn = createIconButton(IconUtils.ICON_ERASER, "tooltip.bookeditor.eraser", b ->
                toggleDrawingTool(DrawingTool.ERASER));
        toolButtonMap.put(DrawingTool.ERASER, eraserBtn);
        section.addWidget(eraserBtn, 18);

        ColorPickerDropdown toolColorBtn = new ColorPickerDropdown(0, 0, argb -> {
            editor.setDrawingToolColor(argb);
            onDirty.run();
        }, 0xFF000000);
        toolColorBtn.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.translatable("tooltip.bookeditor.tool_color")));
        host.addDrawable(toolColorBtn);
        section.addWidget(toolColorBtn, 20);

        NumericTextField toolSizeField = new NumericTextField(host.getTextRenderer(), 0, 0, 36, btnH,
                Text.translatable("tooltip.bookeditor.tool_size"));
        toolSizeField.setText("3");
        toolSizeField.setOnEnterPressed(() -> {
            try {
                int px = Integer.parseInt(toolSizeField.getText().trim());
                px = Math.max(1, Math.min(50, px));
                editor.setToolSize(px);
                toolSizeField.setText(String.valueOf(px));
            } catch (Exception ignored) {}
        });
        host.addDrawable(toolSizeField);
        section.addWidget(toolSizeField, 36);

        sections.add(section);
    }

    private void createCanvasSection() {
        ToolbarSection section = new ToolbarSection("Canvas");

        canvasColorBtn = new ColorPickerDropdown(0, 0, argb -> {
            setCanvasColor.accept(argb);
            editor.markSnapshot();
            onDirty.run();
        }, getCanvasColor.getAsInt());
        canvasColorBtn.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                Text.translatable("tooltip.bookeditor.canvas_color")));
        host.addDrawable(canvasColorBtn);
        section.addWidget(canvasColorBtn, 20);

        sections.add(section);
    }

    private void createPageManagementSection() {
        ToolbarSection section = new ToolbarSection("Pages");

        section.addWidget(createIconButton(IconUtils.ICON_NEW_PAGE, "tooltip.bookeditor.new_page", b -> createNewPage.run()), 18);
        section.addWidget(createIconButton(IconUtils.ICON_DELETE_PAGE, "tooltip.bookeditor.delete_page", b -> deleteCurrentPage.run()), 18);
        section.addWidget(createIconButton(IconUtils.ICON_SIGN, "tooltip.bookeditor.sign", b -> signAction.run()), 18);

        sections.add(section);
    }

    private IconButton createIconButton(Identifier icon, String tooltipKey, ButtonWidget.PressAction action) {
        IconButton button = new IconButton(0, 0, 18, btnH, icon, Text.translatable(tooltipKey), action);
        button.visible = false;
        host.addDrawable(button);
        return button;
    }

    private void distributeToPages() {
        pages.clear();

        int navButtonsWidth = 48;
        int usableWidth = availableWidth - navButtonsWidth - 20;

        List<ToolbarWidget> currentPage = new ArrayList<>();
        int currentWidth = 0;

        for (ToolbarSection section : sections) {
            int sectionWidth = section.getTotalWidth(gap);

            if (currentWidth > 0 && currentWidth + sectionWidth + 10 > usableWidth) {
                pages.add(currentPage);
                currentPage = new ArrayList<>();
                currentWidth = 0;
            }

            if (currentWidth > 0) {
                currentWidth += 10;
            }

            currentPage.addAll(section.widgets);
            currentWidth += sectionWidth;
        }

        if (!currentPage.isEmpty()) {
            pages.add(currentPage);
        }
    }

    private void showPage(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= pages.size()) return;

        currentPage = pageIndex;

        for (ToolbarSection section : sections) {
            for (ToolbarWidget widget : section.widgets) {
                widget.setVisible(false);
            }
        }

        List<ToolbarWidget> pageWidgets = pages.get(pageIndex);
        int cx = x + 48;

        for (ToolbarWidget widget : pageWidgets) {
            widget.setPosition(cx, y);
            widget.setVisible(true);
            cx += widget.width + gap;
        }

        if (prevPageBtn != null) {
            prevPageBtn.active = currentPage > 0;
        }
        if (nextPageBtn != null) {
            nextPageBtn.active = currentPage < pages.size() - 1;
        }

        refreshFormatButtons();
        updateToolHighlights();
    }

    private void toggleDrawingTool(DrawingTool tool) {
        DrawingTool currentTool = editor.getCurrentDrawingTool();

        if (currentTool == tool) {
            editor.deactivateAllTools();
        } else {
            editor.setDrawingTool(tool);
        }

        updateToolHighlights();
        onDirty.run();
    }

    private void updateToolHighlights() {
        DrawingTool currentTool = editor.getCurrentDrawingTool();

        for (Map.Entry<DrawingTool, IconButton> entry : toolButtonMap.entrySet()) {
            IconButton btn = entry.getValue();
            boolean selected = (entry.getKey() == currentTool);
            btn.setSelected(selected);
        }
    }

    public void setVisible(boolean visible) {
        if (prevPageBtn != null) prevPageBtn.visible = visible;
        if (nextPageBtn != null) nextPageBtn.visible = visible;

        if (!visible) {
            for (ToolbarSection section : sections) {
                for (ToolbarWidget widget : section.widgets) {
                    widget.setVisible(false);
                }
            }
        } else {
            showPage(currentPage);
        }
    }

    public void syncWithEditor() {
        editor.syncStylesFromSelection();
        refreshFormatButtons();
        updateToolHighlights();
    }

    public void refreshFormatButtons() {
        if (boldBtn != null) boldBtn.setSelected(editor.isBold());
        if (italicBtn != null) italicBtn.setSelected(editor.isItalic());
        if (underlineBtn != null) underlineBtn.setSelected(editor.isUnderline());
        if (sizeField != null && sizeField.visible) sizeField.setText(String.format("%.1f", editor.getSize()));
        if (textColorBtn != null) textColorBtn.setArgb(editor.getColor());
    }

    public void updateCanvasColor(int color) {
        if (canvasColorBtn != null) {
            canvasColorBtn.setArgb(color);
        }
    }

    public void renderPageIndicator(DrawContext ctx, TextRenderer textRenderer, int screenWidth) {
    }

    private static class ToolbarWidget {
        final Object widget;
        final int width;

        ToolbarWidget(Object widget, int width) {
            this.widget = widget;
            this.width = width;
        }

        void setVisible(boolean visible) {
            if (widget instanceof IconButton btn) {
                btn.visible = visible;
            } else if (widget instanceof NumericTextField field) {
                field.visible = visible;
            } else if (widget instanceof ColorPickerDropdown picker) {
                picker.visible = visible;
            }
        }

        void setPosition(int x, int y) {
            if (widget instanceof IconButton btn) {
                btn.setX(x);
                btn.setY(y);
            } else if (widget instanceof NumericTextField field) {
                field.setX(x);
                field.setY(y);
            } else if (widget instanceof ColorPickerDropdown picker) {
                picker.setX(x);
                picker.setY(y);
            }
        }
    }

    private static class ToolbarSection {
        final String name;
        final List<ToolbarWidget> widgets = new ArrayList<>();

        ToolbarSection(String name) {
            this.name = name;
        }

        void addWidget(Object widget, int width) {
            widgets.add(new ToolbarWidget(widget, width));
        }

        int getTotalWidth(int gap) {
            int width = 0;
            for (int i = 0; i < widgets.size(); i++) {
                if (i > 0) width += gap;
                width += widgets.get(i).width;
            }
            return width;
        }
    }
}