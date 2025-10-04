package bookeditor.client.editor.caret;

import bookeditor.data.BookData;


public class CaretSelectionModel {
    private int caretNode = 0;
    private int caretOffset = 0;

    private boolean selectionActive = false;
    private int selAnchorNode = -1;
    private int selAnchorOffset = 0;

    public void reset() {
        caretNode = 0; caretOffset = 0;
        clearSelection();
    }

    public int getCaretNode() { return caretNode; }
    public int getCaretOffset() { return caretOffset; }
    public void setCaret(int node, int offset) {
        this.caretNode = Math.max(0, node);
        this.caretOffset = Math.max(0, offset);
    }

    public void setAnchor(int node, int offset) {
        this.selAnchorNode = node;
        this.selAnchorOffset = offset;
        this.selectionActive = true;
    }

    public void clearSelection() {
        this.selectionActive = false;
        this.selAnchorNode = -1;
        this.selAnchorOffset = 0;
    }

    public boolean hasSelection() {
        return selectionActive && selAnchorNode >= 0 && !(selAnchorNode == caretNode && selAnchorOffset == caretOffset);
    }

    public int[] selectionStart() {
        if (!hasSelection()) return new int[]{caretNode, caretOffset};
        if (posCmp(selAnchorNode, selAnchorOffset, caretNode, caretOffset) <= 0) return new int[]{selAnchorNode, selAnchorOffset};
        return new int[]{caretNode, caretOffset};
    }

    public int[] selectionEnd() {
        if (!hasSelection()) return new int[]{caretNode, caretOffset};
        if (posCmp(selAnchorNode, selAnchorOffset, caretNode, caretOffset) <= 0) return new int[]{caretNode, caretOffset};
        return new int[]{selAnchorNode, selAnchorOffset};
    }

    public void setSelectionActive(boolean activeWhenDiffers) {
        if (selAnchorNode < 0) selectionActive = false;
        else selectionActive = activeWhenDiffers;
    }

    public void moveLeft(BookData.Page page) {
        if (page == null || page.nodes.isEmpty()) return;
        if (caretNode >= page.nodes.size()) caretNode = page.nodes.size() - 1;
        var n = page.nodes.get(caretNode);
        if (n instanceof BookData.TextNode tn) {
            if (caretOffset > 0) caretOffset--;
            else if (caretNode > 0) {
                caretNode--;
                var prev = page.nodes.get(caretNode);
                caretOffset = (prev instanceof BookData.TextNode pt) ? pt.text.length() : 1;
            }
        } else {
            if (caretOffset > 0) caretOffset = 0;
            else if (caretNode > 0) {
                caretNode--;
                var prev = page.nodes.get(caretNode);
                caretOffset = (prev instanceof BookData.TextNode pt) ? pt.text.length() : 1;
            }
        }
    }

    public void moveRight(BookData.Page page) {
        if (page == null || page.nodes.isEmpty()) return;
        if (caretNode >= page.nodes.size()) caretNode = page.nodes.size() - 1;
        var n = page.nodes.get(caretNode);
        if (n instanceof BookData.TextNode tn) {
            if (caretOffset < tn.text.length()) caretOffset++;
            else if (caretNode < page.nodes.size() - 1) {
                caretNode++;
                caretOffset = (page.nodes.get(caretNode) instanceof BookData.TextNode pt) ? 0 : 1;
            }
        } else {
            if (caretOffset == 0) caretOffset = 1;
            else if (caretNode < page.nodes.size() - 1) {
                caretNode++;
                caretOffset = (page.nodes.get(caretNode) instanceof BookData.TextNode pt) ? 0 : 1;
            }
        }
    }

    public void selectAll(BookData.Page page) {
        if (page == null || page.nodes.isEmpty()) return;
        selAnchorNode = 0; selAnchorOffset = 0;
        caretNode = page.nodes.size() - 1;
        BookData.Node last = page.nodes.get(caretNode);
        caretOffset = (last instanceof BookData.TextNode t) ? t.text.length() : 1;
        selectionActive = true;
    }

    public void ensureWithinPage(BookData.Page page) {
        if (page == null) { caretNode = 0; caretOffset = 0; return; }
        caretNode = Math.max(0, Math.min(caretNode, page.nodes.size() - 1));
        if (caretNode >= 0 && caretNode < page.nodes.size() && page.nodes.get(caretNode) instanceof BookData.TextNode t) {
            caretOffset = Math.max(0, Math.min(caretOffset, t.text.length()));
        } else {
            caretOffset = Math.max(0, Math.min(caretOffset, 1));
        }
    }

    private int posCmp(int n1, int o1, int n2, int o2) {
        if (n1 != n2) return Integer.compare(n1, n2);
        return Integer.compare(o1, o2);
    }
}