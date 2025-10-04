package bookeditor.client.editor.page;

import bookeditor.client.editor.text.StyleParams;
import bookeditor.data.BookData;


public final class PageNormalizer {
    private PageNormalizer() {}

    public static void normalize(BookData.Page page, StyleParams styleDefaults) {
        if (page == null) return;
        if (page.nodes.isEmpty()) {
            page.nodes.add(new BookData.TextNode("", styleDefaults.bold, styleDefaults.italic, styleDefaults.underline, styleDefaults.argb, styleDefaults.size, BookData.ALIGN_LEFT));
            return;
        }
        for (int i = 0; i < page.nodes.size();) {
            BookData.Node n = page.nodes.get(i);
            if (n instanceof BookData.TextNode tn) {
                if (tn.text.isEmpty()) {
                    if (page.nodes.size() > 1) { page.nodes.remove(i); continue; }
                }
                if (i > 0 && page.nodes.get(i - 1) instanceof BookData.TextNode prev) {
                    if (prev.sameStyle(tn)) {
                        prev.text = prev.text + tn.text;
                        page.nodes.remove(i);
                        continue;
                    }
                }
            }
            i++;
        }
    }
}