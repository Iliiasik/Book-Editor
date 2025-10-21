package bookeditor.data.model;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import bookeditor.data.BookDataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TextBoxNodeModel extends NodeModel {
    private static final Logger LOGGER = Logger.getLogger(TextBoxNodeModel.class.getName());

    public int x;
    public int y;
    public int width;
    public int height;
    public int bgArgb = 0x00FFFFFF;
    public final List<TextSegmentModel> segments = new ArrayList<>();

    public TextBoxNodeModel(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public String type() {
        return "textbox";
    }

    @Override
    public NbtCompound toNbt() {
        NbtCompound c = new NbtCompound();
        c.putString("type", "textbox");
        c.putInt("x", x);
        c.putInt("y", y);
        c.putInt("w", width);
        c.putInt("h", height);
        c.putInt("bgArgb", bgArgb);
        NbtList segList = new NbtList();
        int added = 0;
        for (TextSegmentModel seg : segments) {
            if (added >= BookDataUtils.MAX_SEGMENTS_PER_TEXTBOX) {
                break;
            }
            try {
                segList.add(seg.toNbt());
            } catch (RuntimeException ex) {
                LOGGER.log(Level.WARNING, "TextBoxNodeModel: failed to serialize segment, skipping: {0}", ex.getMessage());
            }
            added++;
        }
        c.put("segments", segList);
        return c;
    }

    public static TextBoxNodeModel fromNbt(NbtCompound c) {
        TextBoxNodeModel box = new TextBoxNodeModel(c.getInt("x"), c.getInt("y"), c.getInt("w"), c.getInt("h"));
        box.bgArgb = c.contains("bgArgb", NbtElement.INT_TYPE) ? c.getInt("bgArgb") : 0x00FFFFFF;
        if (c.contains("segments", NbtElement.LIST_TYPE)) {
            NbtList segList = c.getList("segments", NbtElement.COMPOUND_TYPE);
            int limit = Math.min(segList.size(), BookDataUtils.MAX_SEGMENTS_PER_TEXTBOX);
            for (int i = 0; i < limit; i++) {
                try {
                    box.segments.add(TextSegmentModel.fromNbt(segList.getCompound(i)));
                } catch (RuntimeException ex) {
                    LOGGER.log(Level.WARNING, "TextBoxNodeModel: skipped segment {0} due to error: {1}", new Object[]{i, ex.getMessage()});
                }
            }
        }
        return box;
    }
}
