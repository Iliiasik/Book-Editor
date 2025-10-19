package bookeditor.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ImageCache {
    private static final Map<String, Identifier> CACHE = new ConcurrentHashMap<>();
    private static final Map<Identifier, Boolean> LOADED = new ConcurrentHashMap<>();
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "BookEditor-ImageLoader");
        t.setDaemon(true);
        return t;
    });

    public static Identifier getTexture(String url) {
        if (url == null || url.isEmpty()) return null;
        Identifier id = CACHE.get(url);
        if (id == null || !Boolean.TRUE.equals(LOADED.get(id))) return null;
        return id;
    }

    public static void requestTexture(String url) {
        if (url == null || url.isEmpty()) return;
        CACHE.computeIfAbsent(url, ImageCache::download);
    }

    private static Identifier download(String url) {
        String path = "textures/bookeditor/img/" + Integer.toHexString(url.hashCode());
        Identifier id = new Identifier("bookeditor", path);
        EXECUTOR.submit(() -> {
            try (InputStream in = new URL(url).openStream()) {
                NativeImage img = NativeImage.read(in);
                NativeImageBackedTexture tex = new NativeImageBackedTexture(img);
                MinecraftClient.getInstance().execute(() -> {
                    MinecraftClient.getInstance().getTextureManager().registerTexture(id, tex);
                    LOADED.put(id, true);
                });
            } catch (Exception ex) {
                LOADED.put(id, false);
            }
        });
        return id;
    }

    private ImageCache() {}
}