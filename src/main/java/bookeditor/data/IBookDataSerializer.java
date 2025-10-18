package bookeditor.data;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public interface IBookDataSerializer {
    void ensureDefaults(ItemStack stack, PlayerEntity player);
    BookData readFrom(ItemStack stack);
    void writeTo(ItemStack stack, BookData data);
    NbtCompound toNbt(BookData d);
}

