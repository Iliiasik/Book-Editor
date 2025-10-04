package bookeditor.client;

import bookeditor.client.gui.screen.WysiwygBookScreen;
import bookeditor.data.BookData;
import bookeditor.platform.Services;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class BookeditorClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Services.CLIENT = (ItemStack stack, Hand hand) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;
            BookData.ensureDefaults(stack, mc.player);
            mc.setScreen(new WysiwygBookScreen(stack, hand));
        };
    }
}