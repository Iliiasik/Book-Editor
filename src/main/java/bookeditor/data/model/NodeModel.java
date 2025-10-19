package bookeditor.data.model;

import net.minecraft.nbt.NbtCompound;

public abstract class NodeModel {
    public abstract String type();
    public abstract NbtCompound toNbt();

    public static NodeModel fromNbt(NbtCompound c) {
        String t = c.getString("type");
        if ("text".equals(t)) return bookeditor.data.model.TextNodeModel.fromNbt(c);
        if ("image".equals(t)) return bookeditor.data.model.ImageNodeModel.fromNbt(c);
        if ("textbox".equals(t)) return bookeditor.data.model.TextBoxNodeModel.fromNbt(c);
        return bookeditor.data.model.TextNodeModel.fromNbt(c);
    }
}
