package bookeditor.data.model;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;

public class StrokeModel {
    public int color = 0xFF000000;
    public int thickness = 2;
    public final List<Point> points = new ArrayList<>();

    public static class Point {
        public int x;
        public int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

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

    public static StrokeModel fromNbt(NbtCompound c) {
        StrokeModel s = new StrokeModel();
        if (c.contains("color", NbtElement.INT_TYPE)) {
            s.color = c.getInt("color");
        }
        if (c.contains("thickness", NbtElement.INT_TYPE)) {
            s.thickness = c.getInt("thickness");
        }
        NbtList pts = c.getList("points", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < pts.size(); i++) {
            NbtCompound pc = pts.getCompound(i);
            s.points.add(new Point(pc.getInt("x"), pc.getInt("y")));
        }
        return s;
    }
}
