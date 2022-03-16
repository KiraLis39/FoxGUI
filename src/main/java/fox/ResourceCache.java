package fox;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

public class ResourceCache {
    private final static Map<String, Map<Path, byte[]>> cache = Collections.synchronizedMap(new LinkedHashMap<>());

    private final static long MAX_MEMORY = Runtime.getRuntime().maxMemory() - 1L;
    private static long USED_MEMORY, MAX_LOADING;
    private static int MIN_ELEMENTS_CASH_COUNT_TO_CLEARING = 128, MIN_ELEMENTS_BIMAGE_COUNT_TO_CLEARING = 64;
    private static float memGCTrigger = 0.75f;
    private static boolean logEnable = false;

    private ResourceCache() {}


    // опасно заливать память тоннами мусора. Не бойся, мемориКонтроль спасёт тебя =^_^=:
    public static void memoryControl() {
        USED_MEMORY = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        MAX_LOADING = (long) (MAX_MEMORY * memGCTrigger);

        if (USED_MEMORY > MAX_LOADING) {
            log("ResourceManager: Memory control (USED " + (USED_MEMORY / 1048576L) + " > " + ((int) (memGCTrigger * 100)) + "% from MAX " + (MAX_MEMORY / 1048576L) + ")\nClearing...");

            try {
                int clearedCount = 0;
                if (cache.size() > MIN_ELEMENTS_CASH_COUNT_TO_CLEARING) {
                    for (Entry<String, Map<Path, byte[]>> entry : cache.entrySet()) {
                        if (entry.getValue().get(0).length > MAX_LOADING * 0.05f) { // если кэшированная штука занимает больше 5% разрешенной памяти.
                            cache.remove(entry.getKey());
                            clearedCount++;
                        }
                    }

                    log("clearCash: was removed " + clearedCount + " elements from cash.");
                } else {
                    log("clearCash: cash has only " + cache.size() + "elements (MIN_ELEMENTS = " + MIN_ELEMENTS_CASH_COUNT_TO_CLEARING + "), than been full-cleared.");
                    cache.clear(); // если навыбирать ничего не вышло - освобождать что-то же нужно..
                }
            } catch (Exception e) {
                log("Was catched overlap! Hooray! using > " + (USED_MEMORY / 1048576L) + " / " + (MAX_MEMORY / 1048576L) + " >> " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // заливаем новый ресурс:
    public synchronized static void add(File file) throws Exception {
        String name = getFileNameWithoutExt(file.getName());
        add(name, file.toPath());
    }

    public synchronized static void add(URL fileURL) throws Exception {
        String name = getFileNameWithoutExt(new File(fileURL.toURI()).getName());
        add(name, Paths.get(fileURL.getFile()));
    }

    public synchronized static void add(Path file) throws Exception {
        String name = getFileNameWithoutExt(file.toFile().getName());
        add(name, file);
    }

    private synchronized static void add(String index, Path file) throws Exception {
        if (file == null || Files.notExists(file)) {
            throw new RuntimeException("Object file cant be a NULL and should exist!");
        }
        log("Try to add the resource '" + index + "'...");

        Map<Path, byte[]> tmp = new HashMap<>();
        tmp.put(file, Files.readAllBytes(file));
        cache.put(index, tmp);

        memoryControl();
    }

    private static String getFileNameWithoutExt(String name) {
        return name.charAt(name.length() - 4) == '.' ? name.substring(0, name.length() - 4) : name;
    }

    // удаляем ресурс:
    public synchronized static void remove(Object index) {
        String name = String.valueOf(index);
        if (cache.containsKey(name)) {
            cache.remove(name);
        }
    }

    // забираем картинку:
    public synchronized static BufferedImage getBImage(Object index) {
        return getBImage(index, true);
    }

    public synchronized static BufferedImage getBImage(Object index, boolean transparensy) {
        return getBImage(index, transparensy, GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
    }

    public synchronized static BufferedImage getBImage(Object index, boolean transparensy, GraphicsConfiguration gconf) {
        if (gconf == null) {
            gconf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        }

        Objects.requireNonNull(index);
        String name = String.valueOf(index);
        if (name.isEmpty() || name.isBlank()) {
            log("Index of image is empty?");
            return null;
        }

        log("Getting the cashed BufferedImage of " + name);

        ImageIO.setUseCache(false);

        BufferedImage result = null;
        if (cache.containsKey(name)) {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(cache.get(index.toString()).get(0));
                result = ImageIO.read(bais);
            } catch (Exception e) {
                log("Resource '" + name + "' is not writable!");
                return null;
            }
        } else {
            log("Resource '" + name + "' not exist into cache!");
            return null;
        }

        ImageIO.setUseCache(true);
        return result;
    }

    // получить ширину-высоту картинки до её получения отсюда:
    public synchronized static Dimension getBImageDim(Object index) {
        ByteArrayInputStream bais = new ByteArrayInputStream(cache.get(index.toString()).get(0));
        try {
            BufferedImage bim = ImageIO.read(bais);
            return new Dimension(bim.getWidth(), bim.getHeight());
        } catch (Exception e) {
            return null;
        }
    }

    public static Path getFile(String name) {
        if (cache.containsKey(name)) {
            return cache.get(name).keySet().stream().findFirst().get();
        }
        return null;
    }

    // получить массив байтов ресурса:
    public synchronized static byte[] getBytes(Object index) {
        String name = String.valueOf(index);
        if (cache.containsKey(name)) {
            return cache.get(name).get(0);
        } else {
            log("RM.getBytes(): File with name '" + name + "' dont exist into cache!");
            return null;
        }
    }

    public synchronized static void clear() {
        cache.clear();
    }

    public synchronized static int getCacheSize() {
        return cache.size();
    }

    public synchronized static long getCacheLength() {
        long bytesMapSize = 0L;
        for (int i = 0; i < cache.size(); i++) {
            bytesMapSize += cache.get(String.valueOf(i)).get(0).length;
        }
        return bytesMapSize;
    }


    public static void setDebugOn(Boolean enabled) {
        logEnable = enabled;
    }

    public static void setMemoryLoadFactor(float memGCTrigger) {
        ResourceCache.memGCTrigger = memGCTrigger;
    }

    private static void log(String message) {
        if (logEnable) {
            System.out.println(message);
        }
    }


}
