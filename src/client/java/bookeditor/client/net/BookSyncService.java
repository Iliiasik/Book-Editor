package bookeditor.client.net;

import bookeditor.data.BookData;
import bookeditor.data.NbtSizeUtils;
import bookeditor.net.BookNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.nbt.NbtCompound;

public final class BookSyncService {
    private BookSyncService() {}

    public static void sendUpdate(Hand hand, BookData data) {
        NbtCompound nbt = BookData.toNbt(data);

        int nbtSize = NbtSizeUtils.measureNbtInPacket(hand, nbt);
        if (nbtSize < 0) {
            PacketByteBuf tmp = PacketByteBufs.create();
            tmp.writeNbt(nbt);
            nbtSize = tmp.readableBytes();
        }

        int allowed = NbtSizeUtils.getAllowedMax();
        if (nbtSize > allowed) {
            throw new IllegalStateException("Book NBT too large: " + nbtSize + " bytes (allowed " + allowed + ", server max " + PacketByteBuf.MAX_READ_NBT_SIZE + ")");
        }

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeEnumConstant(hand);
        buf.writeNbt(nbt);
        ClientPlayNetworking.send(BookNetworking.UPDATE_BOOK, buf);
    }
}