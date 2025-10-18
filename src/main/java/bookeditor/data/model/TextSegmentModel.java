package bookeditor.data.model;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import bookeditor.data.BookDataUtils;

public class TextSegmentModel {
    public String text;
    public boolean bold;
    public boolean italic;
    public boolean underline;
    public int argb;
    public float size;
    public int align = 0;

    public TextSegmentModel() {}

    public TextSegmentModel(String text, boolean bold, boolean italic, boolean underline, int argb, float size) {
        this.text = text;
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.argb = argb;
        this.size = size;
    }

    public NbtCompound toNbt() {
        NbtCompound c = new NbtCompound();
        c.putString("text", BookDataUtils.safeString(text));
        c.putBoolean("bold", bold);
        c.putBoolean("italic", italic);
        c.putBoolean("underline", underline);
        c.putInt("argb", argb);
        c.putFloat("size", size);
        c.putInt("align", align);
        return c;
    }

    public static TextSegmentModel fromNbt(NbtCompound c) {
        TextSegmentModel seg = new TextSegmentModel();
        seg.text = BookDataUtils.safeString(c.getString("text"));
        seg.bold = c.getBoolean("bold");
        seg.italic = c.getBoolean("italic");
        seg.underline = c.getBoolean("underline");
        seg.argb = c.getInt("argb");
        seg.size = c.getFloat("size");
        seg.align = c.contains("align", NbtElement.INT_TYPE) ? c.getInt("align") : 0;
        return seg;
    }
}
