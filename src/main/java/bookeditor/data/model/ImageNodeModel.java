package bookeditor.data.model;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import bookeditor.data.BookDataUtils;

public class ImageNodeModel extends NodeModel {
    public String url;
    public int w;
    public int h;
    public boolean gif;
    public int align = 0;

    public boolean absolute = true;
    public int x = 0;
    public int y = 0;

    public ImageNodeModel(String url, int w, int h, boolean gif) {
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
        c.putString("url", BookDataUtils.safeString(url));
        c.putInt("w", w);
        c.putInt("h", h);
        c.putBoolean("gif", gif);
        c.putInt("align", align);
        c.putBoolean("abs", absolute);
        c.putInt("x", x);
        c.putInt("y", y);
        return c;
    }

    public static ImageNodeModel fromNbt(NbtCompound c) {
        ImageNodeModel img = new ImageNodeModel(BookDataUtils.safeString(c.getString("url")), c.getInt("w"), c.getInt("h"), c.getBoolean("gif"));
        img.align = c.contains("align", NbtElement.INT_TYPE) ? c.getInt("align") : 0;
        img.absolute = !c.contains("abs", NbtElement.BYTE_TYPE) || c.getBoolean("abs");
        img.x = c.contains("x", NbtElement.INT_TYPE) ? c.getInt("x") : 0;
        img.y = c.contains("y", NbtElement.INT_TYPE) ? c.getInt("y") : 0;
        return img;
    }
}
