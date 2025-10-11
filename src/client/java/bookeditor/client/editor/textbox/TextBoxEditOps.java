package bookeditor.client.editor.textbox;

import bookeditor.client.editor.text.StyleParams;
import bookeditor.data.BookData;

import java.util.ArrayList;
import java.util.List;

public class TextBoxEditOps {

    public void insertChar(BookData.TextBoxNode box, TextBoxCaret caret, StyleParams style, char ch) {
        if (caret.hasSelection()) {
            deleteSelection(box, caret);
        }

        String fullText = box.getFullText();
        int insertIndex = caret.getCharIndex();

        if (insertIndex < 0) insertIndex = 0;
        if (insertIndex > fullText.length()) insertIndex = fullText.length();

        int currentPos = 0;
        for (int i = 0; i < box.segments.size(); i++) {
            BookData.TextSegment seg = box.segments.get(i);
            int segLen = seg.text.length();

            if (insertIndex <= currentPos + segLen) {
                int localIndex = insertIndex - currentPos;

                if (localIndex == 0 && i > 0 && box.segments.get(i - 1).sameStyle(segmentFromStyle(style))) {
                    box.segments.get(i - 1).text += ch;
                } else if (localIndex == segLen && seg.sameStyle(segmentFromStyle(style))) {
                    seg.text = seg.text + ch;
                } else if (seg.sameStyle(segmentFromStyle(style))) {
                    seg.text = seg.text.substring(0, localIndex) + ch + seg.text.substring(localIndex);
                } else {
                    String before = seg.text.substring(0, localIndex);
                    String after = seg.text.substring(localIndex);

                    List<BookData.TextSegment> newSegs = new ArrayList<>();
                    if (!before.isEmpty()) {
                        BookData.TextSegment beforeSeg = seg.copy();
                        beforeSeg.text = before;
                        newSegs.add(beforeSeg);
                    }

                    BookData.TextSegment newSeg = new BookData.TextSegment(
                            String.valueOf(ch), style.bold, style.italic, style.underline, style.argb, style.size
                    );
                    newSeg.align = seg.align;
                    newSegs.add(newSeg);

                    if (!after.isEmpty()) {
                        BookData.TextSegment afterSeg = seg.copy();
                        afterSeg.text = after;
                        newSegs.add(afterSeg);
                    }

                    box.segments.remove(i);
                    box.segments.addAll(i, newSegs);
                }

                caret.setCharIndex(insertIndex + 1);
                caret.clearSelection();
                mergeAdjacentSegments(box);
                return;
            }
            currentPos += segLen;
        }

        if (box.segments.isEmpty() || !box.segments.get(box.segments.size() - 1).sameStyle(segmentFromStyle(style))) {
            BookData.TextSegment newSeg = new BookData.TextSegment(
                    String.valueOf(ch), style.bold, style.italic, style.underline, style.argb, style.size
            );
            newSeg.align = BookData.ALIGN_LEFT;
            box.segments.add(newSeg);
        } else {
            box.segments.get(box.segments.size() - 1).text += ch;
        }

        caret.setCharIndex(insertIndex + 1);
        caret.clearSelection();
        mergeAdjacentSegments(box);
    }

    public void backspace(BookData.TextBoxNode box, TextBoxCaret caret) {
        if (caret.hasSelection()) {
            deleteSelection(box, caret);
            return;
        }

        int deleteIndex = caret.getCharIndex();
        if (deleteIndex <= 0) return;

        deleteCharAt(box, deleteIndex - 1);
        caret.setCharIndex(deleteIndex - 1);
        mergeAdjacentSegments(box);
    }

    public void deleteForward(BookData.TextBoxNode box, TextBoxCaret caret) {
        if (caret.hasSelection()) {
            deleteSelection(box, caret);
            return;
        }

        int deleteIndex = caret.getCharIndex();
        String fullText = box.getFullText();
        if (deleteIndex >= fullText.length()) return;

        deleteCharAt(box, deleteIndex);
        mergeAdjacentSegments(box);
    }

    public void applyStyleToSelection(BookData.TextBoxNode box, TextBoxCaret caret, StyleParams style) {
        if (!caret.hasSelection()) return;

        int selStart = caret.selectionStart();
        int selEnd = caret.selectionEnd();

        List<BookData.TextSegment> newSegments = new ArrayList<>();
        int currentPos = 0;

        for (BookData.TextSegment seg : box.segments) {
            int segStart = currentPos;
            int segEnd = currentPos + seg.text.length();

            if (segEnd <= selStart || segStart >= selEnd) {
                newSegments.add(seg.copy());
            } else {
                int overlapStart = Math.max(segStart, selStart);
                int overlapEnd = Math.min(segEnd, selEnd);

                if (segStart < overlapStart) {
                    BookData.TextSegment before = seg.copy();
                    before.text = seg.text.substring(0, overlapStart - segStart);
                    newSegments.add(before);
                }

                BookData.TextSegment styled = seg.copy();
                styled.text = seg.text.substring(overlapStart - segStart, overlapEnd - segStart);
                styled.bold = style.bold;
                styled.italic = style.italic;
                styled.underline = style.underline;
                styled.argb = style.argb;
                styled.size = style.size;
                newSegments.add(styled);

                if (segEnd > overlapEnd) {
                    BookData.TextSegment after = seg.copy();
                    after.text = seg.text.substring(overlapEnd - segStart);
                    newSegments.add(after);
                }
            }

            currentPos += seg.text.length();
        }

        box.segments.clear();
        box.segments.addAll(newSegments);
        mergeAdjacentSegments(box);
    }

    private void deleteSelection(BookData.TextBoxNode box, TextBoxCaret caret) {
        int selStart = caret.selectionStart();
        int selEnd = caret.selectionEnd();

        for (int i = selEnd - 1; i >= selStart; i--) {
            deleteCharAt(box, i);
        }

        caret.setCharIndex(selStart);
        caret.clearSelection();
        mergeAdjacentSegments(box);
    }

    private void deleteCharAt(BookData.TextBoxNode box, int index) {
        int currentPos = 0;
        for (int i = 0; i < box.segments.size(); i++) {
            BookData.TextSegment seg = box.segments.get(i);
            int segLen = seg.text.length();

            if (index < currentPos + segLen) {
                int localIndex = index - currentPos;
                seg.text = seg.text.substring(0, localIndex) + seg.text.substring(localIndex + 1);

                if (seg.text.isEmpty()) {
                    box.segments.remove(i);
                }
                return;
            }
            currentPos += segLen;
        }
    }

    private void mergeAdjacentSegments(BookData.TextBoxNode box) {
        for (int i = 0; i < box.segments.size() - 1; i++) {
            BookData.TextSegment current = box.segments.get(i);
            BookData.TextSegment next = box.segments.get(i + 1);

            if (current.sameStyle(next)) {
                current.text += next.text;
                box.segments.remove(i + 1);
                i--;
            }
        }
    }

    private BookData.TextSegment segmentFromStyle(StyleParams style) {
        BookData.TextSegment seg = new BookData.TextSegment("", style.bold, style.italic, style.underline, style.argb, style.size);
        seg.align = BookData.ALIGN_LEFT;
        return seg;
    }
}