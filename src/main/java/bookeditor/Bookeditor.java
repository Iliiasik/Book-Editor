package bookeditor;

import bookeditor.item.CreativeBookItem;
import bookeditor.net.BookNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class Bookeditor implements ModInitializer {
    public static final String MODID = "bookeditor";

    public static final Item CREATIVE_BOOK = Registry.register(
            Registries.ITEM,
            new Identifier(MODID, "creative_book"),
            new CreativeBookItem(new Item.Settings().maxCount(1))
    );

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(CREATIVE_BOOK);
        });

        BookNetworking.registerServerReceivers();
    }
}