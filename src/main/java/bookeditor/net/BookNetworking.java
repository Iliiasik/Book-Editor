package bookeditor.net;

import bookeditor.Bookeditor;
import bookeditor.data.BookData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class BookNetworking {
    private static final Logger LOGGER = Logger.getLogger(BookNetworking.class.getName());

    public static final Identifier UPDATE_BOOK = new Identifier(Bookeditor.MODID, "update_book");
    public static final Identifier UPDATE_BOOK_TOO_LARGE = new Identifier(Bookeditor.MODID, "update_book_too_large");

    private BookNetworking() {}

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(UPDATE_BOOK, (server, player, handler, buf, responseSender) -> {
            Hand hand;
            NbtCompound nbt;
            try {
                hand = buf.readEnumConstant(Hand.class);
                try {
                    nbt = buf.readNbt();
                } catch (RuntimeException re) {
                    PacketByteBuf resp = new PacketByteBuf(io.netty.buffer.Unpooled.buffer());
                    resp.writeString("book_nbt_too_large");
                    ServerPlayNetworking.send(player, UPDATE_BOOK_TOO_LARGE, resp);
                    LOGGER.log(Level.WARNING, "BookNetworking: rejected incoming book NBT from player {0} - too large or invalid: {1}", new Object[]{player.getName().getString(), re.getMessage()});
                    return;
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "BookNetworking: failed to parse incoming update packet: " + ex.getMessage(), ex);
                return;
            }

            final Hand finalHand = hand;
            final NbtCompound finalNbt = nbt;
            server.execute(() -> applyOnServer(player, finalHand, finalNbt));
        });
    }

    private static void applyOnServer(ServerPlayerEntity player, Hand hand, NbtCompound nbt) {
        if (nbt == null) return;
        try {
            ItemStack stack = player.getStackInHand(hand);
            if (!stack.isEmpty() && stack.getItem() == Bookeditor.CREATIVE_BOOK) {
                var root = stack.getOrCreateNbt();
                try {
                    root.put(BookData.ROOT, nbt);
                    stack.setNbt(root);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "BookNetworking: failed to apply NBT to item for player " + player.getName().getString() + ": " + ex.getMessage(), ex);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "BookNetworking: unexpected error while applying book NBT: " + ex.getMessage(), ex);
        }
    }
}