package bookeditor.data;

import bookeditor.data.model.*;

public final class BookDataModelAdapter {
    private BookDataModelAdapter() {}

    public static PageModel toModel(BookData.Page p) {
        PageModel pm = new PageModel();
        pm.bgArgb = p.bgArgb;
        for (BookData.Node n : p.nodes) pm.nodes.add(toModel(n));
        for (BookData.Stroke s : p.strokes) pm.strokes.add(toModel(s));
        return pm;
    }

    public static NodeModel toModel(BookData.Node n) {
        if (n instanceof BookData.TextNode) {
            BookData.TextNode tn = (BookData.TextNode) n;
            return new TextNodeModel(tn.text, tn.bold, tn.italic, tn.underline, tn.argb, tn.size, tn.align);
        }
        if (n instanceof BookData.ImageNode) {
            BookData.ImageNode in = (BookData.ImageNode) n;
            ImageNodeModel m = new ImageNodeModel(in.url, in.w, in.h, in.gif);
            m.align = in.align;
            m.absolute = in.absolute;
            m.x = in.x;
            m.y = in.y;
            return m;
        }
        if (n instanceof BookData.TextBoxNode) {
            BookData.TextBoxNode tb = (BookData.TextBoxNode) n;
            TextBoxNodeModel m = new TextBoxNodeModel(tb.x, tb.y, tb.width, tb.height);
            m.bgArgb = tb.bgArgb;
            for (BookData.TextSegment seg : tb.segments) m.segments.add(toModel(seg));
            return m;
        }
        return new TextNodeModel("", false, false, false, 0xFF202020, 1.0f, 0);
    }

    public static TextSegmentModel toModel(BookData.TextSegment s) {
        return new TextSegmentModel(s.text, s.bold, s.italic, s.underline, s.argb, s.size);
    }

    public static StrokeModel toModel(BookData.Stroke s) {
        StrokeModel m = new StrokeModel();
        m.color = s.color;
        m.thickness = s.thickness;
        for (BookData.Stroke.Point p : s.points) m.points.add(new StrokeModel.Point(p.x, p.y));
        return m;
    }

    public static BookData.Page fromModel(PageModel pm) {
        BookData.Page p = new BookData.Page();
        p.bgArgb = pm.bgArgb;
        for (NodeModel nm : pm.nodes) p.nodes.add(fromModel(nm));
        for (StrokeModel sm : pm.strokes) p.strokes.add(fromModel(sm));
        return p;
    }

    public static BookData.Node fromModel(NodeModel nm) {
        if (nm instanceof TextNodeModel) {
            TextNodeModel t = (TextNodeModel) nm;
            return new BookData.TextNode(t.text, t.bold, t.italic, t.underline, t.argb, t.size, t.align);
        }
        if (nm instanceof ImageNodeModel) {
            ImageNodeModel m = (ImageNodeModel) nm;
            BookData.ImageNode in = new BookData.ImageNode(m.url, m.w, m.h, m.gif);
            in.align = m.align;
            in.absolute = m.absolute;
            in.x = m.x;
            in.y = m.y;
            return in;
        }
        if (nm instanceof TextBoxNodeModel) {
            TextBoxNodeModel tb = (TextBoxNodeModel) nm;
            BookData.TextBoxNode box = new BookData.TextBoxNode(tb.x, tb.y, tb.width, tb.height);
            box.bgArgb = tb.bgArgb;
            for (TextSegmentModel seg : tb.segments) box.segments.add(fromModel(seg));
            return box;
        }
        return new BookData.TextNode("", false, false, false, 0xFF202020, 1.0f, 0);
    }

    public static BookData.TextSegment fromModel(TextSegmentModel s) {
        BookData.TextSegment seg = new BookData.TextSegment(s.text, s.bold, s.italic, s.underline, s.argb, s.size);
        seg.align = s.align;
        return seg;
    }

    public static BookData.Stroke fromModel(StrokeModel s) {
        BookData.Stroke st = new BookData.Stroke();
        st.color = s.color;
        st.thickness = s.thickness;
        for (StrokeModel.Point p : s.points) st.points.add(new BookData.Stroke.Point(p.x, p.y));
        return st;
    }
}
