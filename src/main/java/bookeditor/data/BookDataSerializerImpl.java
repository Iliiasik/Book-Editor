package bookeditor.data;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.text.Text;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookDataSerializerImpl implements IBookDataSerializer {

    private static final int MAX_TITLE_LEN = 512;
    private static final int MAX_AUTHOR_LEN = 256;
    private static final int MAX_PAGES = 256;
    private static final Logger LOGGER = Logger.getLogger(BookDataSerializerImpl.class.getName());

    @Override
    public void ensureDefaults(ItemStack stack, PlayerEntity player) {
        NbtCompound root = stack.getOrCreateNbt();
        if (!root.contains(BookData.ROOT, NbtElement.COMPOUND_TYPE)) {
            BookData data = new BookData();
            data.title = Text.translatable("bookeditor.default_title").getString();
            data.authorName = player.getGameProfile().getName();
            data.authorUuid = player.getUuid();
            data.signed = false;
            BookData.Page p = new BookData.Page();
            p.bgArgb = 0xFFF8F8F8;
            BookData.TextBoxNode box = new BookData.TextBoxNode(20, 20, 400, 100);
            box.setText(Text.translatable("bookeditor.default_page").getString(), false, false, false, 0xFF202020, 1.0f);
            p.nodes.add(box);
            data.pages.add(p);
            writeTo(stack, data);
        }
    }

    @Override
    public BookData readFrom(ItemStack stack) {
        BookData d = new BookData();
        try {
            NbtCompound root = stack.getOrCreateNbt();
            if (!root.contains(BookData.ROOT, NbtElement.COMPOUND_TYPE)) return d;
            NbtCompound cb = root.getCompound(BookData.ROOT);

            d.title = BookDataUtils.safeString(cb.getString(BookData.TITLE), MAX_TITLE_LEN);
            d.authorName = BookDataUtils.safeString(cb.getString(BookData.AUTHOR_NAME), MAX_AUTHOR_LEN);
            if (cb.contains(BookData.AUTHOR_UUID, NbtElement.STRING_TYPE)) {
                try { d.authorUuid = UUID.fromString(cb.getString(BookData.AUTHOR_UUID)); } catch (Exception ignored) {}
            }
            d.signed = cb.getBoolean(BookData.SIGNED);

            NbtList pages = cb.getList(BookData.PAGES, NbtElement.COMPOUND_TYPE);
            int pageCount = Math.min(pages.size(), MAX_PAGES);
            for (int i = 0; i < pageCount; i++) {
                try {
                    d.pages.add(BookData.Page.fromNbt(pages.getCompound(i)));
                } catch (RuntimeException ex) {
                    LOGGER.log(Level.WARNING, "BookDataSerializer: skipped page {0} due to error: {1}", new Object[]{i, ex.getMessage()});
                }
            }

        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "BookDataSerializer: failed to read NBT for book item: " + e.getMessage(), e);
            return d;
        }
        return d;
    }

    @Override
    public void writeTo(ItemStack stack, BookData data) {
        NbtCompound root = stack.getOrCreateNbt();
        root.put(BookData.ROOT, toNbt(data));
        stack.setNbt(root);
    }

    @Override
    public NbtCompound toNbt(BookData d) {
        NbtCompound cb = new NbtCompound();
        cb.putString(BookData.TITLE, BookDataUtils.safeString(d.title, MAX_TITLE_LEN));
        cb.putString(BookData.AUTHOR_NAME, BookDataUtils.safeString(d.authorName == null ? "" : d.authorName, MAX_AUTHOR_LEN));
        if (d.authorUuid != null) cb.putString(BookData.AUTHOR_UUID, d.authorUuid.toString());
        cb.putBoolean(BookData.SIGNED, d.signed);

        NbtList pages = new NbtList();
        int added = 0;
        for (BookData.Page p : d.pages) {
            if (added >= MAX_PAGES) break;
            try {
                NbtCompound pn = p.toNbt();
                pages.add(pn);
                added++;
            } catch (RuntimeException ex) {
                LOGGER.log(Level.WARNING, "BookDataSerializer: failed to serialize a page, skipping it: {0}", ex.getMessage());
            }
        }
        cb.put(BookData.PAGES, pages);

        return cb;
    }
}
