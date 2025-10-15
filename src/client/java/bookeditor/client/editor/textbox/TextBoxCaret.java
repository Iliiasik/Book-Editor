package bookeditor.client.editor.textbox;

import bookeditor.data.BookData;

public class TextBoxCaret {
    private int charIndex = 0;
    private int selectionAnchor = -1;

    public void reset() {
        charIndex = 0;
        clearSelection();
    }

    public int getCharIndex() {
        return charIndex;
    }

    public void setCharIndex(int index) {
        this.charIndex = Math.max(0, index);
    }

    public void setAnchor(int index) {
        this.selectionAnchor = index;
    }

    public void clearSelection() {
        this.selectionAnchor = -1;
    }

    public boolean hasSelection() {
        return selectionAnchor >= 0 && selectionAnchor != charIndex;
    }

    public int selectionStart() {
        if (!hasSelection()) return charIndex;
        return Math.min(selectionAnchor, charIndex);
    }

    public int selectionEnd() {
        if (!hasSelection()) return charIndex;
        return Math.max(selectionAnchor, charIndex);
    }

    public void moveLeft(BookData.TextBoxNode textBox) {
        if (textBox == null) return;
        String fullText = textBox.getFullText();
        if (charIndex > 0) {
            charIndex--;
        }
    }

    public void moveRight(BookData.TextBoxNode textBox) {
        if (textBox == null) return;
        String fullText = textBox.getFullText();
        if (charIndex < fullText.length()) {
            charIndex++;
        }
    }

    public void selectAll(BookData.TextBoxNode textBox) {
        if (textBox == null) return;
        selectionAnchor = 0;
        charIndex = textBox.getFullText().length();
    }

    public void ensureWithinBounds(BookData.TextBoxNode textBox) {
        if (textBox == null) {
            charIndex = 0;
            return;
        }
        int maxIndex = textBox.getFullText().length();
        charIndex = Math.max(0, Math.min(charIndex, maxIndex));
    }
}