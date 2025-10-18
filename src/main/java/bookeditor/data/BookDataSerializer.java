package bookeditor.data;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public final class BookDataSerializer {
    public static IBookDataSerializer INSTANCE = new BookDataSerializerImpl();

    private BookDataSerializer() {}

    public static void ensureDefaults(ItemStack stack, PlayerEntity player) {
        INSTANCE.ensureDefaults(stack, player);
    }

    public static BookData readFrom(ItemStack stack) {
        return INSTANCE.readFrom(stack);
    }

    public static void writeTo(ItemStack stack, BookData data) {
        INSTANCE.writeTo(stack, data);
    }

    public static NbtCompound toNbt(BookData d) {
        return INSTANCE.toNbt(d);
    }
}
