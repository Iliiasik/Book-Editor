package bookeditor.net;

import bookeditor.Bookeditor;
import bookeditor.data.BookData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public final class BookNetworking {
    public static final Identifier UPDATE_BOOK = new Identifier(Bookeditor.MODID, "update_book");

    private BookNetworking() {}

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(UPDATE_BOOK, (server, player, handler, buf, responseSender) -> {
            Hand hand = buf.readEnumConstant(Hand.class);
            NbtCompound nbt = buf.readNbt();
            server.execute(() -> applyOnServer(player, hand, nbt));
        });
    }

    private static void applyOnServer(ServerPlayerEntity player, Hand hand, NbtCompound nbt) {
        if (nbt == null) return;
        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isEmpty() && stack.getItem() == Bookeditor.CREATIVE_BOOK) {
            var root = stack.getOrCreateNbt();
            root.put(BookData.ROOT, nbt);
            stack.setNbt(root);
        }
    }
}