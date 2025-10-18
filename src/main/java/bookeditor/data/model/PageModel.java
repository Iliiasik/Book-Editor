package bookeditor.data.model;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import bookeditor.data.BookDataUtils;

import java.util.ArrayList;
import java.util.List;

public class PageModel {
    public final List<NodeModel> nodes = new ArrayList<>();
    public final List<StrokeModel> strokes = new ArrayList<>();
    public int bgArgb = 0xFFF8F8F8;

    public NbtCompound toNbt() {
        NbtCompound c = new NbtCompound();
        NbtList list = new NbtList();
        int addedNodes = 0;
        for (NodeModel n : nodes) {
            if (addedNodes >= BookDataUtils.MAX_NODES_PER_PAGE) break;
            try {
                list.add(n.toNbt());
            } catch (RuntimeException ex) {
                System.err.println("PageModel: failed to serialize node, skipping: " + ex.getMessage());
            }
            addedNodes++;
        }
        c.put("nodes", list);

        NbtList strokesNbt = new NbtList();
        int addedStrokes = 0;
        for (StrokeModel s : strokes) {
            if (addedStrokes >= BookDataUtils.MAX_STROKES_PER_PAGE) break;
            try {
                strokesNbt.add(s.toNbt());
            } catch (RuntimeException ex) {
                System.err.println("PageModel: failed to serialize stroke, skipping: " + ex.getMessage());
            }
            addedStrokes++;
        }
        c.put("strokes", strokesNbt);

        c.putInt("bg", bgArgb);
        return c;
    }

    public static PageModel fromNbt(NbtCompound c) {
        PageModel p = new PageModel();
        NbtList list = c.getList("nodes", NbtElement.COMPOUND_TYPE);
        int nodeLimit = Math.min(list.size(), BookDataUtils.MAX_NODES_PER_PAGE);
        for (int i = 0; i < nodeLimit; i++) {
            try {
                p.nodes.add(NodeModel.fromNbt(list.getCompound(i)));
            } catch (RuntimeException ex) {
                System.err.println("PageModel: skipped node " + i + " due to error: " + ex.getMessage());
            }
        }

        if (c.contains("strokes", NbtElement.LIST_TYPE)) {
            NbtList sList = c.getList("strokes", NbtElement.COMPOUND_TYPE);
            int strokeLimit = Math.min(sList.size(), BookDataUtils.MAX_STROKES_PER_PAGE);
            for (int i = 0; i < strokeLimit; i++) {
                try {
                    p.strokes.add(StrokeModel.fromNbt(sList.getCompound(i)));
                } catch (RuntimeException ex) {
                    System.err.println("PageModel: skipped stroke " + i + " due to error: " + ex.getMessage());
                }
            }
        }
        if (c.contains("bg", NbtElement.INT_TYPE)) p.bgArgb = c.getInt("bg");
        return p;
    }
}
