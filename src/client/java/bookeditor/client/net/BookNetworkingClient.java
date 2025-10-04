package bookeditor.client.net;

import bookeditor.data.BookData;
import bookeditor.net.BookNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;

public final class BookNetworkingClient {
    private BookNetworkingClient() {}

    public static void sendUpdateToServer(Hand hand, BookData data) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeEnumConstant(hand);
        buf.writeNbt(BookData.toNbt(data));
        ClientPlayNetworking.send(BookNetworking.UPDATE_BOOK, buf);
    }
}