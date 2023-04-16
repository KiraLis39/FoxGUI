package fox.utils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MediaCache {
    private static final ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
    private static MediaCache cache;

//    private static long USED_MEMORY, MAX_LOAD_ALLOWED;
//    private final long MAX_MEMORY = Runtime.getRuntime().maxMemory() - 1L;
//    private final int MIN_CASH_SIZE_TO_CLEARING = 128;
//    private final float memGCTrigger = 0.75f;
//    ImageIO.setUseCache(false);

    public static MediaCache getInstance() {
        if (cache == null) {
            cache = new MediaCache();
        }
        return cache;
    }

    public synchronized void addIfAbsent(@NonNull String name, @NonNull Object obj) {
        if (!map.containsKey(name)) {
            map.put(name, obj);
        }
    }

    public Object get(@NonNull String resourceName) {
        if (map.containsKey(resourceName)) {
            return map.get(resourceName);
        }
        return null;
    }

    public void remove(@NonNull String name) {
        map.remove(name);
    }

    public void clear() {
        map.clear();
    }

    /**
     * You can load byte-objects like 'Files.readAllBytes(obj)'
     */
    public synchronized AudioInputStream getAudioStream(String name) throws Exception {
        log.debug("Get the cached AIS of " + name);

        if (map.containsKey(name)) {
            return AudioSystem.getAudioInputStream(new ByteArrayInputStream((byte[]) map.get(name)));
        }

        return null;
    }

    // public synchronized static Cursor getCursor(Object index) throws Exception {
    //        return getCursor(String.valueOf(index), new Point(0, 0));
    //    }

    // public synchronized static Cursor getCursor(Object index, Point dot) throws Exception {
    //        String name = String.valueOf(index);
    //        if (cash.containsKey(name)) {
    //            return toolkit.createCustomCursor(toolkit.createImage(cash.get(name)), dot, name);
    //        } else if (resourseLinksMap.containsKey(name)) {
    //            add(name, resourseLinksMap.get(name), true);
    //            return toolkit.createCustomCursor(toolkit.createImage(cash.get(name)), dot, name);
    //        }
    //
    //        return null;
    //    }

    // public synchronized static BufferedImage getBufferedImage(Object index, Boolean transparensy, GraphicsConfiguration gconf) throws Exception {
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
}
