package bookeditor.data.model;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import bookeditor.data.BookDataUtils;

public class TextNodeModel extends NodeModel {
    public String text;
    public boolean bold, italic, underline;
    public int argb;
    public float size;
    public int align;

    public TextNodeModel() {}

    public TextNodeModel(String text, boolean bold, boolean italic, boolean underline, int argb, float size, int align) {
        this.text = text;
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.argb = argb;
        this.size = size;
        this.align = align;
    }

    @Override
    public String type() {
        return "text";
    }

    @Override
    public NbtCompound toNbt() {
        NbtCompound c = new NbtCompound();
        c.putString("type", "text");
        c.putString("text", BookDataUtils.safeString(text));
        c.putBoolean("bold", bold);
        c.putBoolean("italic", italic);
        c.putBoolean("underline", underline);
        c.putInt("argb", argb);
        c.putFloat("size", size);
        c.putInt("align", align);
        return c;
    }

    public static TextNodeModel fromNbt(NbtCompound c) {
        TextNodeModel t = new TextNodeModel();
        t.text = BookDataUtils.safeString(c.getString("text"));
        t.bold = c.getBoolean("bold");
        t.italic = c.getBoolean("italic");
        t.underline = c.getBoolean("underline");
        t.argb = c.contains("argb", NbtElement.INT_TYPE) ? c.getInt("argb") : 0xFF202020;
        t.size = c.contains("size", NbtElement.FLOAT_TYPE) ? c.getFloat("size") : 1.0f;
        t.align = c.contains("align", NbtElement.INT_TYPE) ? BookDataUtils.clampAlign(c.getInt("align")) : 0;
        return t;
    }
}
