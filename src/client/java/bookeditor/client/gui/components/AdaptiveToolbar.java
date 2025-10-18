package bookeditor.client.gui.components;

import bookeditor.client.editor.tools.DrawingTool;
import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.widget.button.ColorPickerDropdown;
import bookeditor.client.gui.widget.button.IconButton;
import bookeditor.client.gui.widget.field.NumericTextField;
import bookeditor.client.gui.widget.editor.EditorWidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.function.Consumer;

public class AdaptiveToolbar {
    private final WidgetHost host;
    private final EditorWidget editor;
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

    private ToolbarPager pager;

    private final List<ToolbarSection> sections = new ArrayList<>();

    private IconButton boldBtn;
    private IconButton italicBtn;
    private IconButton underlineBtn;
    private NumericTextField sizeField;
    private ColorPickerDropdown textColorBtn;
    private ColorPickerDropdown canvasColorBtn;

    private final Map<DrawingTool, IconButton> toolButtonMap = new HashMap<>();

    public AdaptiveToolbar(WidgetHost host, EditorWidget editor, Runnable onDirty,
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
        pager = new ToolbarPager(host, x, y, btnH, gap, availableWidth, p -> {
            refreshFormatButtons();
            updateToolHighlights();
        });

        createSections();
        pager.setSections(sections);
        pager.showPage(0);
    }

    private void createSections() {
        sections.clear();

        SectionBuildResult res;

        res = new HistorySectionBuilder().build(host, editor, onDirty, getCanvasColor, setCanvasColor, openInsertDialog, createNewPage, deleteCurrentPage, signAction, btnH);
        sections.add(res.section);

        res = new TextStyleSectionBuilder().build(host, editor, onDirty, getCanvasColor, setCanvasColor, openInsertDialog, createNewPage, deleteCurrentPage, signAction, btnH);
        sections.add(res.section);
        boldBtn = (IconButton) res.get("boldBtn");
        italicBtn = (IconButton) res.get("italicBtn");
        underlineBtn = (IconButton) res.get("underlineBtn");
        textColorBtn = (ColorPickerDropdown) res.get("textColorBtn");

        res = new TextSizeSectionBuilder().build(host, editor, onDirty, getCanvasColor, setCanvasColor, openInsertDialog, createNewPage, deleteCurrentPage, signAction, btnH);
        sections.add(res.section);
        sizeField = (NumericTextField) res.get("sizeField");

        res = new AlignmentSectionBuilder().build(host, editor, onDirty, getCanvasColor, setCanvasColor, openInsertDialog, createNewPage, deleteCurrentPage, signAction, btnH);
        sections.add(res.section);

        res = new ContentSectionBuilder().build(host, editor, onDirty, getCanvasColor, setCanvasColor, openInsertDialog, createNewPage, deleteCurrentPage, signAction, btnH);
        sections.add(res.section);

        res = new DrawingToolsSectionBuilder().build(host, editor, onDirty, getCanvasColor, setCanvasColor, openInsertDialog, createNewPage, deleteCurrentPage, signAction, btnH);
        sections.add(res.section);
        Object o;
        o = res.get("brushBtn"); if (o instanceof IconButton) toolButtonMap.put(DrawingTool.BRUSH, (IconButton)o);
        o = res.get("sprayBtn"); if (o instanceof IconButton) toolButtonMap.put(DrawingTool.SPRAY, (IconButton)o);
        o = res.get("lineBtn"); if (o instanceof IconButton) toolButtonMap.put(DrawingTool.LINE, (IconButton)o);
        o = res.get("rectangleBtn"); if (o instanceof IconButton) toolButtonMap.put(DrawingTool.RECTANGLE, (IconButton)o);
        o = res.get("circleBtn"); if (o instanceof IconButton) toolButtonMap.put(DrawingTool.CIRCLE, (IconButton)o);
        o = res.get("eraserBtn"); if (o instanceof IconButton) toolButtonMap.put(DrawingTool.ERASER, (IconButton)o);

        res = new CanvasSectionBuilder().build(host, editor, onDirty, getCanvasColor, setCanvasColor, openInsertDialog, createNewPage, deleteCurrentPage, signAction, btnH);
        sections.add(res.section);
        canvasColorBtn = (ColorPickerDropdown) res.get("canvasColorBtn");

        res = new PageManagementSectionBuilder().build(host, editor, onDirty, getCanvasColor, setCanvasColor, openInsertDialog, createNewPage, deleteCurrentPage, signAction, btnH);
        sections.add(res.section);
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
        if (pager != null) pager.setVisible(visible);
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

}
