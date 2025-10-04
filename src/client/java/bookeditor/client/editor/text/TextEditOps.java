package bookeditor.client.editor.text;

import bookeditor.client.editor.caret.CaretSelectionModel;
import bookeditor.client.editor.page.PageNormalizer;
import bookeditor.data.BookData;


public class TextEditOps {

    public void insertChar(BookData.Page page, CaretSelectionModel caret, StyleParams style, char chr) {
        if (chr == '\r') chr = '\n';
        if (page.nodes.isEmpty()) {
            page.nodes.add(new BookData.TextNode("", style.bold, style.italic, style.underline, style.argb, style.size, BookData.ALIGN_LEFT));
            caret.setCaret(0, 0);
        }
        if (caret.getCaretNode() >= page.nodes.size()) caret.setCaret(page.nodes.size() - 1, caret.getCaretOffset());

        if (page.nodes.get(caret.getCaretNode()) instanceof BookData.TextNode tn) {
            BookData.TextNode left = tn.copy();
            String l = left.text.substring(0, Math.min(caret.getCaretOffset(), left.text.length()));
            String r = left.text.substring(Math.min(caret.getCaretOffset(), left.text.length()));
            left.text = l;
            BookData.TextNode right = tn.copy();
            right.text = r;

            int idx = caret.getCaretNode();
            page.nodes.remove(idx);
            if (!left.text.isEmpty()) page.nodes.add(idx++, left);

            BookData.TextNode mid = new BookData.TextNode("", style.bold, style.italic, style.underline, style.argb, style.size, tn.align);
            mid.text = String.valueOf(chr);
            page.nodes.add(idx, mid);

            if (!right.text.isEmpty()) page.nodes.add(idx + 1, right);

            caret.setCaret(Math.min(idx + 1, page.nodes.size() - 1), 1);
        } else {
            int insertIndex = caret.getCaretNode() + (caret.getCaretOffset() > 0 ? 1 : 0);
            BookData.TextNode mid = new BookData.TextNode("", style.bold, style.italic, style.underline, style.argb, style.size, BookData.ALIGN_LEFT);
            mid.text = String.valueOf(chr);
            page.nodes.add(insertIndex, mid);
            caret.setCaret(insertIndex, 1);
        }

        caret.clearSelection();
        PageNormalizer.normalize(page, style);
    }

    public void deleteSelection(BookData.Page page, CaretSelectionModel caret, StyleParams style) {
        if (!caret.hasSelection()) return;
        int[] st = caret.selectionStart();
        int[] en = caret.selectionEnd();
        int sN = st[0], sO = st[1];
        int eN = en[0], eO = en[1];

        if (sN == eN) {
            if (sN >= 0 && sN < page.nodes.size() && page.nodes.get(sN) instanceof BookData.TextNode tn) {
                tn.text = tn.text.substring(0, Math.min(sO, tn.text.length())) + tn.text.substring(Math.min(eO, tn.text.length()));
                caret.setCaret(sN, Math.min(sO, tn.text.length()));
            }
        } else {
            if (sN >= 0 && sN < page.nodes.size() && page.nodes.get(sN) instanceof BookData.TextNode sT &&
                    eN >= 0 && eN < page.nodes.size() && page.nodes.get(eN) instanceof BookData.TextNode eT) {
                String left = sT.text.substring(0, Math.min(sO, sT.text.length()));
                String right = eT.text.substring(Math.min(eO, eT.text.length()));
                sT.text = left;
                for (int i = eN; i >= sN + 1; i--) page.nodes.remove(i);
                if (!right.isEmpty()) {
                    BookData.TextNode tail = eT.copy();
                    tail.text = right;
                    page.nodes.add(sN + 1, tail);
                }
                caret.setCaret(sN, sT.text.length());
            } else {
                for (int i = eN; i >= sN; i--) page.nodes.remove(i);
                if (page.nodes.isEmpty()) page.nodes.add(new BookData.TextNode("", style.bold, style.italic, style.underline, style.argb, style.size, BookData.ALIGN_LEFT));
                int node = Math.min(sN, page.nodes.size() - 1);
                int off = (page.nodes.get(node) instanceof BookData.TextNode t) ? Math.min(sO, t.text.length()) : 1;
                caret.setCaret(node, off);
            }
        }
        caret.clearSelection();
        PageNormalizer.normalize(page, style);
    }

    public void backspace(BookData.Page page, CaretSelectionModel caret, StyleParams style) {
        if (page.nodes.isEmpty()) {
            page.nodes.add(new BookData.TextNode("", style.bold, style.italic, style.underline, style.argb, style.size, BookData.ALIGN_LEFT));
            caret.setCaret(0,0); return;
        }
        var n = page.nodes.get(caret.getCaretNode());
        if (n instanceof BookData.TextNode tn) {
            if (caret.getCaretOffset() > 0 && caret.getCaretOffset() <= tn.text.length()) {
                tn.text = tn.text.substring(0, caret.getCaretOffset() - 1) + tn.text.substring(caret.getCaretOffset());
                caret.setCaret(caret.getCaretNode(), Math.max(0, caret.getCaretOffset() - 1));
                PageNormalizer.normalize(page, style);
            } else if (caret.getCaretOffset() == 0 && caret.getCaretNode() > 0) {
                var prev = page.nodes.get(caret.getCaretNode() - 1);
                if (prev instanceof BookData.TextNode pt) {
                    int prevLen = pt.text.length();
                    if (pt.sameStyle(tn)) {
                        pt.text = pt.text + tn.text;
                        page.nodes.remove(caret.getCaretNode());
                        caret.setCaret(caret.getCaretNode() - 1, prevLen);
                    } else {
                        if (prevLen > 0) {
                            pt.text = pt.text.substring(0, prevLen - 1);
                            caret.setCaret(caret.getCaretNode() - 1, pt.text.length());
                        } else {
                            page.nodes.remove(caret.getCaretNode() - 1);
                            caret.setCaret(caret.getCaretNode() - 1, 0);
                        }
                    }
                    PageNormalizer.normalize(page, style);
                }
            }
        }
    }

    public void deleteForward(BookData.Page page, CaretSelectionModel caret, StyleParams style) {
        if (page.nodes.isEmpty()) return;
        var n = page.nodes.get(caret.getCaretNode());
        if (n instanceof BookData.TextNode tn) {
            if (caret.getCaretOffset() < tn.text.length()) {
                tn.text = tn.text.substring(0, caret.getCaretOffset()) + tn.text.substring(caret.getCaretOffset() + 1);
                PageNormalizer.normalize(page, style);
            } else if (caret.getCaretNode() < page.nodes.size() - 1) {
                var next = page.nodes.get(caret.getCaretNode() + 1);
                if (next instanceof BookData.TextNode nt) {
                    if (tn.sameStyle(nt)) {
                        tn.text = tn.text + nt.text;
                        page.nodes.remove(caret.getCaretNode() + 1);
                    } else if (!nt.text.isEmpty()) {
                        nt.text = nt.text.substring(1);
                    } else {
                        page.nodes.remove(caret.getCaretNode() + 1);
                    }
                    PageNormalizer.normalize(page, style);
                }
            }
        }
    }
}