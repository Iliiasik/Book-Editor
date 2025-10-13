package bookeditor.client.gui.components;

import bookeditor.client.editor.tools.DrawingTool;
import bookeditor.client.gui.base.WidgetHost;
import bookeditor.client.gui.widget.ColorPickerDropdown;
import bookeditor.client.gui.widget.IconButton;
import bookeditor.client.gui.widget.NumericTextField;
import bookeditor.client.gui.widget.RichTextEditorWidget;
import bookeditor.client.util.IconUtils;
import net.minecraft.client.gui.DrawContext;
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

    private IconButton textBoxBtn;
    private IconButton imgBtn;
    private ColorPickerDropdown textBoxBgColorBtn;

    private IconButton brushBtn;
    private IconButton sprayBtn;
    private IconButton lineBtn;
    private IconButton rectangleBtn;
    private IconButton circleBtn;
    private IconButton eraserBtn;

    private ColorPickerDropdown toolColorBtn;
    private NumericTextField toolSizeField;

    private ColorPickerDropdown canvasColorBtn;
    private IconButton newPageBtn, deletePageBtn;
    private IconButton signBtn;

    private int textEndX;
    private int toolsEndX;
    private int canvasEndX;
    private int pagesEndX;

    private boolean compactMode = false;

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

    public void setCompactMode(boolean compact) {
        this.compactMode = compact;
    }

    public void build(int initialCanvasArgb) {
        int cx = x;

        textBoxBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_TEXTBOX,
                Text.translatable("tooltip.bookeditor.textbox"), b -> {
            editor.activateTextBoxTool();
            onDirty.run();
        });
        host.addDrawable(textBoxBtn);
        cx += 18 + gap;

        if (!compactMode) {
            imgBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_IMAGE,
                    Text.translatable("tooltip.bookeditor.image"), b -> openInsertDialog.run());
            host.addDrawable(imgBtn);
            cx += 18 + gap;
        }

        textBoxBgColorBtn = new ColorPickerDropdown(cx, y, argb -> {
            editor.setTextBoxBgColor(argb);
            onDirty.run();
        }, 0x00FFFFFF);
        textBoxBgColorBtn.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.translatable("tooltip.bookeditor.textbox_bg_color")));
        host.addDrawable(textBoxBgColorBtn);
        cx += 20 + 10;
        textEndX = cx;

        brushBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_BRUSH,
                Text.translatable("tooltip.bookeditor.brush"), b -> {
            if (editor.getCurrentDrawingTool() == DrawingTool.BRUSH) {
                editor.deactivateAllTools();
            } else {
                editor.setDrawingTool(DrawingTool.BRUSH);
            }
            updateToolHighlight();
            onDirty.run();
        });
        host.addDrawable(brushBtn);
        cx += 18 + gap;

        if (!compactMode) {
            sprayBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_SPRAY,
                    Text.translatable("tooltip.bookeditor.spray"), b -> {
                if (editor.getCurrentDrawingTool() == DrawingTool.SPRAY) {
                    editor.deactivateAllTools();
                } else {
                    editor.setDrawingTool(DrawingTool.SPRAY);
                }
                updateToolHighlight();
                onDirty.run();
            });
            host.addDrawable(sprayBtn);
            cx += 18 + gap;
        }

        lineBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_LINE,
                Text.translatable("tooltip.bookeditor.line"), b -> {
            if (editor.getCurrentDrawingTool() == DrawingTool.LINE) {
                editor.deactivateAllTools();
            } else {
                editor.setDrawingTool(DrawingTool.LINE);
            }
            updateToolHighlight();
            onDirty.run();
        });
        host.addDrawable(lineBtn);
        cx += 18 + gap;

        if (!compactMode) {
            rectangleBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_RECTANGLE,
                    Text.translatable("tooltip.bookeditor.rectangle"), b -> {
                if (editor.getCurrentDrawingTool() == DrawingTool.RECTANGLE) {
                    editor.deactivateAllTools();
                } else {
                    editor.setDrawingTool(DrawingTool.RECTANGLE);
                }
                updateToolHighlight();
                onDirty.run();
            });
            host.addDrawable(rectangleBtn);
            cx += 18 + gap;

            circleBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_CIRCLE,
                    Text.translatable("tooltip.bookeditor.circle"), b -> {
                if (editor.getCurrentDrawingTool() == DrawingTool.CIRCLE) {
                    editor.deactivateAllTools();
                } else {
                    editor.setDrawingTool(DrawingTool.CIRCLE);
                }
                updateToolHighlight();
                onDirty.run();
            });
            host.addDrawable(circleBtn);
            cx += 18 + gap;
        }

        eraserBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_ERASER,
                Text.translatable("tooltip.bookeditor.eraser"), b -> {
            if (editor.getCurrentDrawingTool() == DrawingTool.ERASER) {
                editor.deactivateAllTools();
            } else {
                editor.setDrawingTool(DrawingTool.ERASER);
            }
            updateToolHighlight();
            onDirty.run();
        });
        host.addDrawable(eraserBtn);
        cx += 18 + gap;

        toolColorBtn = new ColorPickerDropdown(cx, y, argb -> {
            editor.setDrawingToolColor(argb);
            onDirty.run();
        }, 0xFF000000);
        toolColorBtn.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.translatable("tooltip.bookeditor.tool_color")));
        host.addDrawable(toolColorBtn);
        cx += 20 + gap;

        toolSizeField = new NumericTextField(host.getTextRenderer(), cx, y, 36, btnH, Text.translatable("tooltip.bookeditor.tool_size"));
        toolSizeField.setText("3");
        toolSizeField.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.translatable("tooltip.bookeditor.tool_size_range")));
        toolSizeField.setOnEnterPressed(() -> {
            try {
                int px = Integer.parseInt(toolSizeField.getText().trim());
                px = Math.max(1, Math.min(50, px));
                editor.setToolSize(px);
                toolSizeField.setText(String.valueOf(px));
            } catch (Exception ignored) {
            }
        });
        host.addDrawable(toolSizeField);
        cx += 36 + 10;
        toolsEndX = cx;

        canvasColorBtn = new ColorPickerDropdown(cx, y, argb -> {
            onCanvasColorChanged.accept(argb);
            editor.markSnapshot();
            onDirty.run();
        }, initialCanvasArgb);
        canvasColorBtn.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.translatable("tooltip.bookeditor.canvas_color")));
        host.addDrawable(canvasColorBtn);
        cx += 20 + 10;
        canvasEndX = cx;

        newPageBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_NEW_PAGE,
                Text.translatable("tooltip.bookeditor.new_page"), b -> createNewPage.run());
        host.addDrawable(newPageBtn);
        cx += 18 + gap;

        deletePageBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_DELETE_PAGE,
                Text.translatable("tooltip.bookeditor.delete_page"), b -> deleteCurrentPage.run());
        host.addDrawable(deletePageBtn);
        cx += 18 + gap;

        if (!compactMode) {
            signBtn = new IconButton(cx, y, 18, btnH, IconUtils.ICON_SIGN,
                    Text.translatable("tooltip.bookeditor.sign"), b -> signAction.run());
            host.addDrawable(signBtn);
            cx += 18;
        }
        pagesEndX = cx;

        updateToolHighlight();
    }

    public void renderSectionHeaders(DrawContext ctx, int textColor) {
        if (compactMode) return;

        int labelY = y - 14;

        ctx.drawText(host.getTextRenderer(), Text.translatable("toolbar.bookeditor.text"),
                x, labelY, textColor, false);

        ctx.drawText(host.getTextRenderer(), Text.translatable("toolbar.bookeditor.tools"),
                textEndX, labelY, textColor, false);

        ctx.drawText(host.getTextRenderer(), Text.translatable("toolbar.bookeditor.canvas"),
                toolsEndX, labelY, textColor, false);

        ctx.drawText(host.getTextRenderer(), Text.translatable("toolbar.bookeditor.pages"),
                canvasEndX, labelY, textColor, false);
    }

    public void renderSectionBoxes(DrawContext ctx) {
        if (compactMode) return;

        int boxY = y - 2;
        int boxH = btnH + 4;

        ctx.fill(x - 2, boxY, textEndX - 5, boxY + boxH, 0x33FFFFFF);
        ctx.fill(textEndX - 2, boxY, toolsEndX - 5, boxY + boxH, 0x33FFFFFF);
        ctx.fill(toolsEndX - 2, boxY, canvasEndX - 5, boxY + boxH, 0x33FFFFFF);
        ctx.fill(canvasEndX - 2, boxY, pagesEndX + 2, boxY + boxH, 0x33FFFFFF);
    }

    public void updateToolHighlight() {
        DrawingTool currentTool = editor.getCurrentDrawingTool();

        if (brushBtn != null) brushBtn.setSelected(currentTool == DrawingTool.BRUSH);
        if (sprayBtn != null) sprayBtn.setSelected(currentTool == DrawingTool.SPRAY);
        if (lineBtn != null) lineBtn.setSelected(currentTool == DrawingTool.LINE);
        if (rectangleBtn != null) rectangleBtn.setSelected(currentTool == DrawingTool.RECTANGLE);
        if (circleBtn != null) circleBtn.setSelected(currentTool == DrawingTool.CIRCLE);
        if (eraserBtn != null) eraserBtn.setSelected(currentTool == DrawingTool.ERASER);
    }

    public void setVisible(boolean v, boolean signed) {
        if (textBoxBtn != null) textBoxBtn.visible = v;
        if (imgBtn != null) imgBtn.visible = v;
        if (textBoxBgColorBtn != null) textBoxBgColorBtn.visible = v;
        if (brushBtn != null) brushBtn.visible = v;
        if (sprayBtn != null) sprayBtn.visible = v;
        if (lineBtn != null) lineBtn.visible = v;
        if (rectangleBtn != null) rectangleBtn.visible = v;
        if (circleBtn != null) circleBtn.visible = v;
        if (eraserBtn != null) eraserBtn.visible = v;
        if (toolColorBtn != null) toolColorBtn.visible = v;
        if (toolSizeField != null) toolSizeField.visible = v;
        if (canvasColorBtn != null) canvasColorBtn.visible = v;
        if (newPageBtn != null) newPageBtn.visible = v;
        if (deletePageBtn != null) deletePageBtn.visible = v;
        if (signBtn != null) signBtn.visible = v && !signed;
    }

    public void setCanvasColor(int argb) {
        if (canvasColorBtn != null) canvasColorBtn.setArgb(argb);
    }

    public void setToolSize(int px) {
        if (toolSizeField != null) toolSizeField.setText(Integer.toString(px));
        editor.setToolSize(px);
    }

    public void setTextBoxBgColor(int argb) {
        if (textBoxBgColorBtn != null) textBoxBgColorBtn.setArgb(argb);
    }

    public void setToolColor(int argb) {
        if (toolColorBtn != null) toolColorBtn.setArgb(argb);
    }
}