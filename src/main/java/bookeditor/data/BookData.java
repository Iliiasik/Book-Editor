package bookeditor.data;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
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
            return BookDataModelAdapter.toModel(this).toNbt();
        }

        public static Page fromNbt(NbtCompound c) {
            return BookDataModelAdapter.fromModel(bookeditor.data.model.PageModel.fromNbt(c));
        }
    }

    public static abstract class Node {
        public abstract String type();
        public abstract NbtCompound toNbt();

        public static Node fromNbt(NbtCompound c) {
            return BookDataModelAdapter.fromModel(bookeditor.data.model.NodeModel.fromNbt(c));
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
            return BookDataModelAdapter.toModel(this).toNbt();
        }

        public static TextNode fromNbt(NbtCompound c) {
            return (TextNode) BookDataModelAdapter.fromModel(bookeditor.data.model.NodeModel.fromNbt(c));
        }

        public TextNode copy() {
            return new TextNode(text, bold, italic, underline, argb, size, align);
        }

        public boolean sameStyle(TextNode other) {
            if (other == null) return false;
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
        public int bgArgb = 0x00FFFFFF;
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
            return BookDataModelAdapter.toModel(this).toNbt();
        }

        public static TextBoxNode fromNbt(NbtCompound c) {
            return (TextBoxNode) BookDataModelAdapter.fromModel(bookeditor.data.model.NodeModel.fromNbt(c));
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
            return BookDataModelAdapter.toModel(this).toNbt();
        }

        public static TextSegment fromNbt(NbtCompound c) {
            return BookDataModelAdapter.fromModel(bookeditor.data.model.TextSegmentModel.fromNbt(c));
        }

        public TextSegment copy() {
            TextSegment seg = new TextSegment(text, bold, italic, underline, argb, size);
            seg.align = align;
            return seg;
        }

        public boolean sameStyle(TextSegment other) {
            if (other == null) return false;
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
            return BookDataModelAdapter.toModel(this).toNbt();
        }

        public static ImageNode fromNbt(NbtCompound c) {
            return (ImageNode) BookDataModelAdapter.fromModel(bookeditor.data.model.NodeModel.fromNbt(c));
        }
    }

    public static class Stroke {
        public int color = 0xFF000000;
        public int thickness = 2;
        public final List<Point> points = new ArrayList<>();
        public static class Point { public int x,y; public Point(int x, int y){ this.x=x; this.y=y; } }

        public NbtCompound toNbt() {
            return BookDataModelAdapter.toModel(this).toNbt();
        }
        public static Stroke fromNbt(NbtCompound c) {
            return (Stroke) BookDataModelAdapter.fromModel(bookeditor.data.model.StrokeModel.fromNbt(c));
        }
    }

    public static void ensureDefaults(ItemStack stack, PlayerEntity player) {
        BookDataSerializer.ensureDefaults(stack, player);
    }

    public static BookData readFrom(ItemStack stack) {
        return BookDataSerializer.readFrom(stack);
    }

    public static void writeTo(ItemStack stack, BookData data) {
        BookDataSerializer.writeTo(stack, data);
    }

    public static NbtCompound toNbt(BookData d) {
        return BookDataSerializer.toNbt(d);
    }
}