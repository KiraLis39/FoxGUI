package fox.utils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class MediaCache {
    private static final ConcurrentHashMap<Object, byte[]> map = new ConcurrentHashMap<>(32);
    private MediaCache cache;

    public synchronized static Cursor getCursor(Object index) {
        return getCursor(index, new Point(0, 0));
    }

    public synchronized static Cursor getCursor(Object index, Point dot) {
        if (map.containsKey(index)) {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            return toolkit.createCustomCursor(toolkit.createImage(map.get(index)), dot, String.valueOf(index));
        }
        return null;
    }

    public MediaCache getInstance() {
        if (cache == null) {
            cache = new MediaCache();
        }
        return cache;
    }

    public byte[] getResourceBytes(@NonNull Object resourceName) {
        if (map.containsKey(resourceName)) {
            return map.get(resourceName);
        }
        return null;
    }

    public synchronized AudioInputStream getAudioStream(Object name) {
        log.debug("Get the cached AIS of " + name);

        if (map.containsKey(name)) {
            try {
                return AudioSystem.getAudioInputStream(new ByteArrayInputStream(map.get(name)));
            } catch (UnsupportedAudioFileException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public InputStream getResourceStream(@NonNull Object key) {
        if (map.containsKey(key) && map.get(key) != null) {
            return new ByteArrayInputStream(map.get(key));
        }
        log.error("Не найдено в кэше файла с ключом '{}'", key);
        return null;
    }

    public BufferedImage getBufferedImage(Object key) {
        try {
            return ImageIO.read(new ByteArrayInputStream(map.get(key)));
        } catch (Exception e) {
            log.error("Image reading error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * @param key any object as resource marker into cache (as example - String)
     * @param obj some bytes array like Files.readAllBytes(obj)
     */
    public synchronized void addIfAbsent(@NonNull Object key, @NonNull byte[] obj) {
        map.putIfAbsent(key, obj);
    }

    /**
     * @param key  any object as resource marker into cache (as example - String)
     * @param link url to remote resource location
     * @param type is a DATA_TYPE enum of resource type for auto extension add (like .wav or .png)
     */
    public synchronized void addRemoteIfAbsent(Object key, String link, DATA_TYPE type) {
        try {
            link += type.extension;
            if (Files.notExists(Path.of(link))) {
                log.error("File {} is NULL or empty or wrong path '{}'", key, link);
                return;
            }
            map.put(key, Files.readAllBytes(Path.of(link)));
        } catch (Exception e) {
            log.error("Ошибка обработки файла '{}': {}", key, e.getMessage());
        }
    }

    /**
     * @param key               any object as resource marker into cache (as example - String)
     * @param localResourceLink url to local class resource
     * @param type              is a DATA_TYPE enum of resource type for auto extension add (like .wav or .png)
     */
    public synchronized void addLocalIfAbsent(Object key, String localResourceLink, DATA_TYPE type) {
        try (InputStream is = MediaCache.class.getResourceAsStream(localResourceLink + type.extension)) {
            if (is == null || is.available() <= 0) {
                log.error("InputStream {} is NULL or empty", key);
                return;
            }
            map.put(key, is.readAllBytes());
        } catch (Exception e) {
            log.error("Ошибка обработки файла '{}': {}", key, e.getMessage());
        }
    }

    public boolean hasKey(String key) {
        return map.containsKey(key);
    }

    public void remove(@NonNull String name) {
        map.remove(name);
    }

    public void clear() {
        map.clear();
    }

    // private synchronized static BufferedImage getBufferedImage(Object index, Boolean transparensy, GraphicsConfiguration gconf) throws Exception {
    //        // OPAQUE = 1; BITMASK = 2; TRANSLUCENT = 3;
    //        int transparensyInt = 3;
    //
    //        String name = String.valueOf(index);
    //        ImageIO.setUseCache(false);
    //        BufferedImage tmp;
    //        log.debug("Getting the cashed BufferedImage of " + name);
    //
    //        /*
    //         * Если в буфере уже есть имейдж - возвращаем его.
    //         * Иначе, заливаем и удаляем из кеша.
    //         *
    //         *  Т.к. изображение должно быть ИЛИ в кэше,
    //         *  ИЛИ в буфере. Не одновременно, для экономии памяти.
    //         */
    //        if (imageBuffer.containsKey(name)) {
    //            return imageBuffer.get(name);
    //        } else {
    //            if (cash.containsKey(name)) {
    //                // если в кэше есть файл, который нужно трансформировать в имейдж:
    //                tmp = gconf.createCompatibleImage(
    //                        ImageIO.read(new ByteArrayInputStream(cash.get(name))).getWidth(),
    //                        ImageIO.read(new ByteArrayInputStream(cash.get(name))).getHeight(),
    //                        transparensyInt
    //                );
    //
    //                if (transparensy) {
    //                    tmp.getGraphics().drawImage(ImageIO.read(new ByteArrayInputStream(cash.get(name))), 0, 0, null);
    //                    imageBuffer.put(name, tmp);
    //                } else {
    //                    imageBuffer.put(name, ImageIO.read(new ByteArrayInputStream(cash.get(name))));
    //                }
    //
    //                cash.remove(name);
    //            } else {
    //                if (resourseLinksMap.containsKey(name)) {
    //                    // если в кэше его нет, но есть ссылка на файл, который нужно прочитать как имейдж:
    //                    tmp = gconf.createCompatibleImage(
    //                            ImageIO.read(resourseLinksMap.get(name)).getWidth(),
    //                            ImageIO.read(resourseLinksMap.get(name)).getHeight(),
    //                            transparensyInt
    //                    );
    //
    //                    if (transparensy) {
    //                        tmp.getGraphics().drawImage(ImageIO.read(resourseLinksMap.get(name)), 0, 0, null);
    //                        imageBuffer.put(name, tmp);
    //                    } else {
    //                        imageBuffer.put(name, ImageIO.read(resourseLinksMap.get(name)));
    //                    }
    //                } else {
    //                    // Ничего не найдено. Не зарегистрировано:
    //                    log.debug("BufferedImage '" + name + "' not exist into ResourceManager!");
    //                    return null;
    //                }
    //            }
    //        }
    //
    //        return imageBuffer.get(name);
    //    }

//    private void memoryControl() {
//        USED_MEMORY = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
//        MAX_LOAD_ALLOWED = (long) (MAX_MEMORY * memGCTrigger);
//
//        if (USED_MEMORY > MAX_LOAD_ALLOWED) {
//            Print(getClass(), Out.LEVEL.DEBUG, "MediaCache: Memory control (USED " + (USED_MEMORY / 1048576L) + " > " +
//                    ((int) (memGCTrigger * 100)) + "% from MAX " + (MAX_MEMORY / 1048576L) + ")\n Than clearing...");
//
//            int clearedCount = 0;
//            try {
//                if (map.size() > MIN_CASH_SIZE_TO_CLEARING) {
//                    for (int i = 0; i < MIN_CASH_SIZE_TO_CLEARING * 0.05; i++) {
//                        map.remove(i);
//                    }
//                    Print(getClass(), Out.LEVEL.DEBUG, "memoryControl: was removed " + clearedCount + " elements from cash.");
//                } else {
//                    Print(getClass(), Out.LEVEL.DEBUG, "memoryControl: cash size is only " + map.size() + " (MIN_CASH_SIZE = " +
//                            MIN_CASH_SIZE_TO_CLEARING + "), than been full-cleared.");
//                    cache.clear();
//                }
//            } catch (Exception e) {
//                Print(getClass(), Out.LEVEL.DEBUG, "Was caught overlap! Hooray! using > " +
//                        (USED_MEMORY / 1048576L) + " / " + (MAX_MEMORY / 1048576L) + " >> " + e.getMessage());
//                e.printStackTrace();
//            }
//        }
//    }

    public enum DATA_TYPE {
        WAV(".wav"),
        PNG(".png");

        private final String extension;

        DATA_TYPE(String extension) {
            this.extension = extension;
        }
    }

    //    private static long USED_MEMORY, MAX_LOAD_ALLOWED;
    //    private final long MAX_MEMORY = Runtime.getRuntime().maxMemory() - 1L;
    //    private final int MIN_CASH_SIZE_TO_CLEARING = 128;
    //    private final float memGCTrigger = 0.75f;
    //    ImageIO.setUseCache(false);
}
