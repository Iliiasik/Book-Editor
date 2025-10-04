package bookeditor.platform;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public final class Services {
    public interface ClientActions {
        void openCreativeBook(ItemStack stack, Hand hand);
    }
    public static ClientActions CLIENT = (stack, hand) -> {};
    private Services() {}
}