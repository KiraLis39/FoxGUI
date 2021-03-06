package utils;

import lombok.NonNull;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MediaCache {
    private final static ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
    private static MediaCache cache;
//    private static long USED_MEMORY, MAX_LOAD_ALLOWED;
//    private final long MAX_MEMORY = Runtime.getRuntime().maxMemory() - 1L;
//    private final int MIN_CASH_SIZE_TO_CLEARING = 128;
//    private final float memGCTrigger = 0.75f;
//    ImageIO.setUseCache(false);

    private MediaCache() {}

    public static MediaCache getInstance() {
        if (cache == null) {
            cache = new MediaCache();
        }
        return cache;
    }

    public void addIfAbsent(@NonNull String name, @NonNull Object mustCached) {
        if (!map.containsKey(name)) {
            map.put(name, mustCached);
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
