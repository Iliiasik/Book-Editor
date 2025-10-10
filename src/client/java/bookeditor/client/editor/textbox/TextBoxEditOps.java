package bookeditor.client.editor.textbox;

import bookeditor.client.editor.text.StyleParams;
import bookeditor.data.BookData;

import java.util.ArrayList;
import java.util.List;

public class TextBoxEditOps {

    public void insertChar(BookData.TextBoxNode textBox, TextBoxCaret caret, StyleParams style, char chr) {
        if (textBox == null) return;
        if (chr == '\r') chr = '\n';

        if (caret.hasSelection()) {
            deleteSelection(textBox, caret);
        }

        String fullText = textBox.getFullText();
        int insertPos = Math.min(caret.getCharIndex(), fullText.length());

        StringBuilder newText = new StringBuilder(fullText);
        newText.insert(insertPos, chr);

        rebuildSegments(textBox, newText.toString(), insertPos, insertPos + 1, style);
        caret.setCharIndex(insertPos + 1);
        caret.clearSelection();
    }

    public void deleteSelection(BookData.TextBoxNode textBox, TextBoxCaret caret) {
        if (textBox == null || !caret.hasSelection()) return;

        int start = caret.selectionStart();
        int end = caret.selectionEnd();
        String fullText = textBox.getFullText();

        String newText = fullText.substring(0, start) + fullText.substring(end);
        rebuildSegmentsPreserveStyle(textBox, newText);

        caret.setCharIndex(start);
        caret.clearSelection();
    }

    public void backspace(BookData.TextBoxNode textBox, TextBoxCaret caret) {
        if (textBox == null) return;

        if (caret.hasSelection()) {
            deleteSelection(textBox, caret);
            return;
        }

        int pos = caret.getCharIndex();
        if (pos > 0) {
            String fullText = textBox.getFullText();
            String newText = fullText.substring(0, pos - 1) + fullText.substring(pos);
            rebuildSegmentsPreserveStyle(textBox, newText);
            caret.setCharIndex(pos - 1);
        }
    }

    public void deleteForward(BookData.TextBoxNode textBox, TextBoxCaret caret) {
        if (textBox == null) return;

        if (caret.hasSelection()) {
            deleteSelection(textBox, caret);
            return;
        }

        String fullText = textBox.getFullText();
        int pos = caret.getCharIndex();
        if (pos < fullText.length()) {
            String newText = fullText.substring(0, pos) + fullText.substring(pos + 1);
            rebuildSegmentsPreserveStyle(textBox, newText);
        }
    }

    public void applyStyleToSelection(BookData.TextBoxNode textBox, TextBoxCaret caret, StyleParams style) {
        if (textBox == null || !caret.hasSelection()) return;

        int start = caret.selectionStart();
        int end = caret.selectionEnd();

        List<BookData.TextSegment> newSegments = new ArrayList<>();
        int currentIndex = 0;

        for (BookData.TextSegment seg : textBox.segments) {
            int segStart = currentIndex;
            int segEnd = currentIndex + seg.text.length();

            if (segEnd <= start || segStart >= end) {
                newSegments.add(seg.copy());
            } else {
                if (segStart < start) {
                    String beforeText = seg.text.substring(0, start - segStart);
                    newSegments.add(new BookData.TextSegment(beforeText, seg.bold, seg.italic, seg.underline, seg.argb, seg.size));
                }

                int overlapStart = Math.max(0, start - segStart);
                int overlapEnd = Math.min(seg.text.length(), end - segStart);
                String styledText = seg.text.substring(overlapStart, overlapEnd);
                newSegments.add(new BookData.TextSegment(
                        styledText,
                        style.bold,
                        style.italic,
                        style.underline,
                        style.argb,
                        style.size
                ));

                if (segEnd > end) {
                    String afterText = seg.text.substring(end - segStart);
                    newSegments.add(new BookData.TextSegment(afterText, seg.bold, seg.italic, seg.underline, seg.argb, seg.size));
                }
            }

            currentIndex = segEnd;
        }

        textBox.segments.clear();
        textBox.segments.addAll(newSegments);
        normalizeSegments(textBox);
    }

    private void rebuildSegments(BookData.TextBoxNode textBox, String newText, int changeStart, int changeEnd, StyleParams style) {
        List<BookData.TextSegment> newSegments = new ArrayList<>();
        int currentIndex = 0;

        for (BookData.TextSegment seg : textBox.segments) {
            int segEnd = currentIndex + seg.text.length();

            if (segEnd <= changeStart) {
                newSegments.add(seg.copy());
                currentIndex = segEnd;
            } else if (currentIndex >= changeStart) {
                break;
            } else {
                String beforeText = seg.text.substring(0, changeStart - currentIndex);
                newSegments.add(new BookData.TextSegment(beforeText, seg.bold, seg.italic, seg.underline, seg.argb, seg.size));
                break;
            }
        }

        String insertedText = newText.substring(changeStart, Math.min(changeEnd, newText.length()));
        if (!insertedText.isEmpty()) {
            newSegments.add(new BookData.TextSegment(insertedText, style.bold, style.italic, style.underline, style.argb, style.size));
        }

        currentIndex = 0;
        for (BookData.TextSegment seg : textBox.segments) {
            int segEnd = currentIndex + seg.text.length();
            if (segEnd > changeStart) {
                int remainStart = Math.max(0, changeStart - currentIndex);
                if (remainStart < seg.text.length()) {
                    String remainText = newText.substring(Math.min(changeEnd, newText.length()));
                    if (!remainText.isEmpty()) {
                        newSegments.add(new BookData.TextSegment(remainText, seg.bold, seg.italic, seg.underline, seg.argb, seg.size));
                    }
                }
                break;
            }
            currentIndex = segEnd;
        }

        textBox.segments.clear();
        textBox.segments.addAll(newSegments);
        normalizeSegments(textBox);
    }

    private void rebuildSegmentsPreserveStyle(BookData.TextBoxNode textBox, String newText) {
        if (newText.isEmpty()) {
            textBox.segments.clear();
            return;
        }

        List<BookData.TextSegment> oldSegments = new ArrayList<>(textBox.segments);
        List<BookData.TextSegment> newSegments = new ArrayList<>();

        int newIndex = 0;
        int oldIndex = 0;

        for (BookData.TextSegment oldSeg : oldSegments) {
            int oldSegLen = oldSeg.text.length();
            int available = newText.length() - newIndex;

            if (available <= 0) break;

            int takeLen = Math.min(oldSegLen, available);
            String segText = newText.substring(newIndex, newIndex + takeLen);
            newSegments.add(new BookData.TextSegment(segText, oldSeg.bold, oldSeg.italic, oldSeg.underline, oldSeg.argb, oldSeg.size));

            newIndex += takeLen;
            oldIndex += oldSegLen;
        }

        if (newIndex < newText.length() && !oldSegments.isEmpty()) {
            BookData.TextSegment lastStyle = oldSegments.get(oldSegments.size() - 1);
            String remaining = newText.substring(newIndex);
            newSegments.add(new BookData.TextSegment(remaining, lastStyle.bold, lastStyle.italic, lastStyle.underline, lastStyle.argb, lastStyle.size));
        }

        textBox.segments.clear();
        textBox.segments.addAll(newSegments);
        normalizeSegments(textBox);
    }

    private void normalizeSegments(BookData.TextBoxNode textBox) {
        if (textBox.segments.isEmpty()) return;

        List<BookData.TextSegment> normalized = new ArrayList<>();
        BookData.TextSegment current = null;

        for (BookData.TextSegment seg : textBox.segments) {
            if (seg.text.isEmpty()) continue;

            if (current == null) {
                current = seg.copy();
            } else if (current.sameStyle(seg)) {
                current.text += seg.text;
            } else {
                normalized.add(current);
                current = seg.copy();
            }
        }

        if (current != null) {
            normalized.add(current);
        }

        textBox.segments.clear();
        textBox.segments.addAll(normalized);
    }
}