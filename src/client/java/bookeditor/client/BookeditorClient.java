package bookeditor.client;

import bookeditor.client.gui.screen.BookScreen;
import bookeditor.data.BookData;
import bookeditor.platform.Services;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class BookeditorClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Services.CLIENT = (ItemStack stack, Hand hand) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;
            BookData.ensureDefaults(stack, mc.player);
            mc.setScreen(new BookScreen(stack, hand));
        };

        Item creativeBook = Registries.ITEM.get(new Identifier("bookeditor", "creative_book"));
        ModelPredicateProviderRegistry.register(
                creativeBook,
                new Identifier("bookeditor", "signed"),
                (stack, world, entity, seed) -> {
                    BookData d = BookData.readFrom(stack);
                    return d.signed ? 1.0f : 0.0f;
                }
        );
    }
}