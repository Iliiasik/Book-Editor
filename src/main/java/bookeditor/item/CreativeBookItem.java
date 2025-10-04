package bookeditor.item;

import bookeditor.data.BookData;
import bookeditor.platform.Services;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class CreativeBookItem extends Item {
    public CreativeBookItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        user.incrementStat(Stats.USED.getOrCreateStat(this));

        if (world.isClient) {
            BookData.ensureDefaults(stack, user);
            BookData d = BookData.readFrom(stack);
            if (d.signed && d.title != null && !d.title.isEmpty()) {
                stack.setCustomName(Text.literal(d.title));
            }
            Services.CLIENT.openCreativeBook(stack, hand);
        }

        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        BookData d = BookData.readFrom(stack);
        if (d.signed) {
            if (d.authorName != null && !d.authorName.isEmpty()) {
                tooltip.add(Text.literal(d.authorName).setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.GRAY)));
            }
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    public static Text getDisplayName() {
        return Text.translatable("item.bookeditor.creative_book");
    }
}