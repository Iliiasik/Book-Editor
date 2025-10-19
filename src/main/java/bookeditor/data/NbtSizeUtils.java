package bookeditor.data;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;

public final class NbtSizeUtils {
    private NbtSizeUtils() {}

    public static final int CLIENT_SAFE_NBT_MARGIN = 64;

    public static int getAllowedMax() {
        return PacketByteBuf.MAX_READ_NBT_SIZE - CLIENT_SAFE_NBT_MARGIN;
    }

    public static int getNbtByteSize(NbtCompound compound) {
        if (compound == null) return 1;
        try {
            PacketByteBuf tmp = PacketByteBufs.create();
            tmp.writeNbt(compound);
            return tmp.readableBytes();
        } catch (RuntimeException e) {
            return -1;
        }
    }

    public static int measureNbtInPacket(Hand hand, NbtCompound compound) {
        if (compound == null) return 1;
        try {
            PacketByteBuf tmp = PacketByteBufs.create();
            tmp.writeEnumConstant(hand);
            int afterEnum = tmp.readableBytes();
            tmp.writeNbt(compound);
            int afterNbt = tmp.readableBytes();
            return afterNbt - afterEnum;
        } catch (RuntimeException e) {
            return getNbtByteSize(compound);
        }
    }
}
