package bookeditor.util;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

public final class SkullStackUtil {
    public static ItemStack playerHeadStack(String name, UUID uuid) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        NbtCompound tag = stack.getOrCreateNbt();
        NbtCompound owner = new NbtCompound();
        if (uuid != null) owner.putUuid("Id", uuid);
        if (name != null) owner.putString("Name", name);
        tag.put("SkullOwner", owner);
        stack.setNbt(tag);
        return stack;
    }

    private SkullStackUtil(){}
}