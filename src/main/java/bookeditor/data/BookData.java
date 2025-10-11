package bookeditor.data;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BookData {

    public static final String ROOT = "CreativeBook";
    public static final String PAGES = "pages";
    public static final String TITLE = "title";
    public static final String AUTHOR_NAME = "authorName";
    public static final String AUTHOR_UUID = "authorUuid";
    public static final String SIGNED = "signed";

    public String title = "";
    public String authorName = "";
    public UUID authorUuid = Util.NIL_UUID;
    public boolean signed = false;
    public final List<Page> pages = new ArrayList<>();

    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 2;

    public static class Page {
        public final List<Node> nodes = new ArrayList<>();
        public final List<Stroke> strokes = new ArrayList<>();
        public int bgArgb = 0xFFF8F8F8;

        public NbtCompound toNbt() {
            NbtCompound c = new NbtCompound();

            NbtList list = new NbtList();
            for (Node n : nodes) list.add(n.toNbt());
            c.put("nodes", list);

            NbtList strokesNbt = new NbtList();
            for (Stroke s : strokes) strokesNbt.add(s.toNbt());
            c.put("strokes", strokesNbt);

            c.putInt("bg", bgArgb);
            return c;
        }

        public static Page fromNbt(NbtCompound c) {
            Page p = new Page();
            NbtList list = c.getList("nodes", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) p.nodes.add(Node.fromNbt(list.getCompound(i)));

            if (c.contains("strokes", NbtElement.LIST_TYPE)) {
                NbtList sList = c.getList("strokes", NbtElement.COMPOUND_TYPE);
                for (int i = 0; i < sList.size(); i++) p.strokes.add(Stroke.fromNbt(sList.getCompound(i)));
            }
            if (c.contains("bg", NbtElement.INT_TYPE)) p.bgArgb = c.getInt("bg");
            return p;
        }
    }

    public static abstract class Node {
        public abstract String type();
        public abstract NbtCompound toNbt();

        public static Node fromNbt(NbtCompound c) {
            String t = c.getString("type");
            if ("text".equals(t)) return TextNode.fromNbt(c);
            if ("image".equals(t)) return ImageNode.fromNbt(c);
            if ("textbox".equals(t)) return TextBoxNode.fromNbt(c);
            return new TextNode("", false, false, false, 0xFF202020, 1.0f, ALIGN_LEFT);
        }
    }

    public static class TextNode extends Node {
        public String text;
        public boolean bold, italic, underline;
        public int argb;
        public float size;
        public int align;

        public TextNode(String text, boolean bold, boolean italic, boolean underline, int argb, float size, int align) {
            this.text = text;
            this.bold = bold;
            this.italic = italic;
            this.underline = underline;
            this.argb = argb;
            this.size = size;
            this.align = align;
        }

        @Override public String type() { return "text"; }

        @Override
        public NbtCompound toNbt() {
            NbtCompound c = new NbtCompound();
            c.putString("type", "text");
            c.putString("text", text);
            c.putBoolean("bold", bold);
            c.putBoolean("italic", italic);
            c.putBoolean("underline", underline);
            c.putInt("argb", argb);
            c.putFloat("size", size);
            c.putInt("align", align);
            return c;
        }

        public static TextNode fromNbt(NbtCompound c) {
            return new TextNode(
                    c.getString("text"),
                    c.getBoolean("bold"),
                    c.getBoolean("italic"),
                    c.getBoolean("underline"),
                    c.contains("argb", NbtElement.INT_TYPE) ? c.getInt("argb") : 0xFF202020,
                    c.contains("size", NbtElement.FLOAT_TYPE) ? c.getFloat("size") : 1.0f,
                    c.contains("align", NbtElement.INT_TYPE) ? clampAlign(c.getInt("align")) : ALIGN_LEFT
            );
        }

        public TextNode copy() {
            return new TextNode(text, bold, italic, underline, argb, size, align);
        }

        public boolean sameStyle(TextNode other) {
            return this.bold == other.bold &&
                    this.italic == other.italic &&
                    this.underline == other.underline &&
                    this.argb == other.argb &&
                    Float.compare(this.size, other.size) == 0 &&
                    this.align == other.align;
        }
    }

    public static class TextBoxNode extends Node {
        public int x;
        public int y;
        public int width;
        public int height;
        public final List<TextSegment> segments = new ArrayList<>();

        public TextBoxNode(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override public String type() { return "textbox"; }

        @Override
        public NbtCompound toNbt() {
            NbtCompound c = new NbtCompound();
            c.putString("type", "textbox");
            c.putInt("x", x);
            c.putInt("y", y);
            c.putInt("w", width);
            c.putInt("h", height);

            NbtList segList = new NbtList();
            for (TextSegment seg : segments) segList.add(seg.toNbt());
            c.put("segments", segList);

            return c;
        }

        public static TextBoxNode fromNbt(NbtCompound c) {
            TextBoxNode box = new TextBoxNode(
                    c.getInt("x"),
                    c.getInt("y"),
                    c.getInt("w"),
                    c.getInt("h")
            );

            if (c.contains("segments", NbtElement.LIST_TYPE)) {
                NbtList segList = c.getList("segments", NbtElement.COMPOUND_TYPE);
                for (int i = 0; i < segList.size(); i++) {
                    box.segments.add(TextSegment.fromNbt(segList.getCompound(i)));
                }
            }

            return box;
        }

        public String getFullText() {
            StringBuilder sb = new StringBuilder();
            for (TextSegment seg : segments) {
                sb.append(seg.text);
            }
            return sb.toString();
        }

        public void setText(String text, boolean bold, boolean italic, boolean underline, int argb, float size) {
            segments.clear();
            if (!text.isEmpty()) {
                segments.add(new TextSegment(text, bold, italic, underline, argb, size));
            }
        }
    }

    public static class TextSegment {
        public String text;
        public boolean bold;
        public boolean italic;
        public boolean underline;
        public int argb;
        public float size;
        public int align = ALIGN_LEFT;

        public TextSegment(String text, boolean bold, boolean italic, boolean underline, int argb, float size) {
            this.text = text;
            this.bold = bold;
            this.italic = italic;
            this.underline = underline;
            this.argb = argb;
            this.size = size;
        }

        public NbtCompound toNbt() {
            NbtCompound c = new NbtCompound();
            c.putString("text", text);
            c.putBoolean("bold", bold);
            c.putBoolean("italic", italic);
            c.putBoolean("underline", underline);
            c.putInt("argb", argb);
            c.putFloat("size", size);
            c.putInt("align", align);
            return c;
        }

        public static TextSegment fromNbt(NbtCompound c) {
            TextSegment seg = new TextSegment(
                    c.getString("text"),
                    c.getBoolean("bold"),
                    c.getBoolean("italic"),
                    c.getBoolean("underline"),
                    c.getInt("argb"),
                    c.getFloat("size")
            );
            seg.align = c.contains("align", NbtElement.INT_TYPE) ? clampAlign(c.getInt("align")) : ALIGN_LEFT;
            return seg;
        }

        public TextSegment copy() {
            TextSegment seg = new TextSegment(text, bold, italic, underline, argb, size);
            seg.align = align;
            return seg;
        }

        public boolean sameStyle(TextSegment other) {
            return this.bold == other.bold &&
                    this.italic == other.italic &&
                    this.underline == other.underline &&
                    this.argb == other.argb &&
                    Float.compare(this.size, other.size) == 0 &&
                    this.align == other.align;
        }
    }

    public static class ImageNode extends Node {
        public String url;
        public int w;
        public int h;
        public boolean gif;
        public int align = ALIGN_LEFT;

        public boolean absolute = true;
        public int x = 0;
        public int y = 0;

        public ImageNode(String url, int w, int h, boolean gif) {
            this.url = url;
            this.w = w;
            this.h = h;
            this.gif = gif;
        }

        @Override public String type() { return "image"; }

        @Override
        public NbtCompound toNbt() {
            NbtCompound c = new NbtCompound();
            c.putString("type", "image");
            c.putString("url", url == null ? "" : url);
            c.putInt("w", w);
            c.putInt("h", h);
            c.putBoolean("gif", gif);
            c.putInt("align", align);
            c.putBoolean("abs", absolute);
            c.putInt("x", x);
            c.putInt("y", y);
            return c;
        }

        public static ImageNode fromNbt(NbtCompound c) {
            ImageNode img = new ImageNode(c.getString("url"), c.getInt("w"), c.getInt("h"), c.getBoolean("gif"));
            img.align = c.contains("align", NbtElement.INT_TYPE) ? clampAlign(c.getInt("align")) : ALIGN_LEFT;
            img.absolute = c.contains("abs", NbtElement.BYTE_TYPE) ? c.getBoolean("abs") : true;
            img.x = c.contains("x", NbtElement.INT_TYPE) ? c.getInt("x") : 0;
            img.y = c.contains("y", NbtElement.INT_TYPE) ? c.getInt("y") : 0;
            return img;
        }
    }

    public static class Stroke {
        public int color = 0xFF000000;
        public int thickness = 2;
        public final List<Point> points = new ArrayList<>();
        public static class Point { public int x,y; public Point(int x, int y){ this.x=x; this.y=y; } }

        public NbtCompound toNbt() {
            NbtCompound c = new NbtCompound();
            c.putInt("color", color);
            c.putInt("thickness", thickness);
            NbtList pts = new NbtList();
            for (Point p : points) {
                NbtCompound pc = new NbtCompound();
                pc.putInt("x", p.x);
                pc.putInt("y", p.y);
                pts.add(pc);
            }
            c.put("points", pts);
            return c;
        }
        public static Stroke fromNbt(NbtCompound c) {
            Stroke s = new Stroke();
            if (c.contains("color", NbtElement.INT_TYPE)) s.color = c.getInt("color");
            if (c.contains("thickness", NbtElement.INT_TYPE)) s.thickness = c.getInt("thickness");
            NbtList pts = c.getList("points", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < pts.size(); i++) {
                NbtCompound pc = pts.getCompound(i);
                s.points.add(new Point(pc.getInt("x"), pc.getInt("y")));
            }
            return s;
        }
    }

    private static int clampAlign(int a) {
        if (a < 0) return ALIGN_LEFT;
        if (a > 2) return ALIGN_RIGHT;
        return a;
    }

    public static void ensureDefaults(ItemStack stack, PlayerEntity player) {
        NbtCompound root = stack.getOrCreateNbt();
        if (!root.contains(ROOT, NbtElement.COMPOUND_TYPE)) {
            BookData data = new BookData();
            data.title = Text.translatable("bookeditor.default_title").getString();
            data.authorName = player.getGameProfile().getName();
            data.authorUuid = player.getUuid();
            data.signed = false;
            Page p = new Page();
            p.bgArgb = 0xFFF8F8F8;
            TextBoxNode box = new TextBoxNode(20, 20, 400, 100);
            box.setText(Text.translatable("bookeditor.default_page").getString(), false, false, false, 0xFF202020, 1.0f);
            p.nodes.add(box);
            data.pages.add(p);
            writeTo(stack, data);
        }
    }

    public static BookData readFrom(ItemStack stack) {
        BookData d = new BookData();
        NbtCompound root = stack.getOrCreateNbt();
        if (!root.contains(ROOT, NbtElement.COMPOUND_TYPE)) return d;
        NbtCompound cb = root.getCompound(ROOT);
        d.title = cb.getString(TITLE);
        d.authorName = cb.getString(AUTHOR_NAME);
        if (cb.contains(AUTHOR_UUID, NbtElement.STRING_TYPE)) {
            try { d.authorUuid = UUID.fromString(cb.getString(AUTHOR_UUID)); } catch (Exception ignored) {}
        }
        d.signed = cb.getBoolean(SIGNED);
        NbtList pages = cb.getList(PAGES, NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < pages.size(); i++) d.pages.add(Page.fromNbt(pages.getCompound(i)));
        return d;
    }

    public static void writeTo(ItemStack stack, BookData data) {
        NbtCompound root = stack.getOrCreateNbt();
        root.put(ROOT, toNbt(data));
        stack.setNbt(root);
    }

    public static NbtCompound toNbt(BookData d) {
        NbtCompound cb = new NbtCompound();
        cb.putString(TITLE, d.title);
        cb.putString(AUTHOR_NAME, d.authorName == null ? "" : d.authorName);
        if (d.authorUuid != null) cb.putString(AUTHOR_UUID, d.authorUuid.toString());
        cb.putBoolean(SIGNED, d.signed);

        NbtList pages = new NbtList();
        for (Page p : d.pages) pages.add(p.toNbt());
        cb.put(PAGES, pages);

        return cb;
    }
}