package fox.utils;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Deprecated
public class ResourceManager {
    private final static long MAX_MEMORY = Runtime.getRuntime().maxMemory();
    private static final Toolkit toolkit = Toolkit.getDefaultToolkit();
    //or:
    private static final Map<String, byte[]> cash = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final Map<String, BufferedImage> imageBuffer = Collections.synchronizedMap(new LinkedHashMap<>());
    //and:
    private static final Map<String, File> resourseLinksMap = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final int MIN_ELEMENTS_CASH_COUNT_TO_CLEARING = 100;
    private static long USED_MEMORY;


    // may be overrides:
    public static void memoryControl() {
        USED_MEMORY = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        long MAX_LOADING = MAX_MEMORY / 100 * 90;

        if (USED_MEMORY >= MAX_LOADING) {
            log.debug("ResourceManager: Memory control activation. Clearing the cash...");

            try {
                clearCash();
            } catch (Exception e) {
                log.debug("Was catched memory overlap! Hooray! usingLong > " + (USED_MEMORY / 1048576L) + " / " + (MAX_MEMORY / 1048576L) + " >> " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    public synchronized static void clearCash() {
        int clearedCount = 0;
        log.debug(ResourceManager.class.getName(), 1, "clearCash: clearing the cash by current memory using is " + USED_MEMORY + "/" + MAX_MEMORY);
        if (cash.size() > MIN_ELEMENTS_CASH_COUNT_TO_CLEARING) {
            for (Map.Entry<String, byte[]> entry : cash.entrySet()) {
                if (entry.getValue().length > MAX_MEMORY / 10) {
                    cash.remove(entry.getKey());
                    clearedCount++;
                }
            }
            log.debug(ResourceManager.class.getName(), 1, "clearCash: was removed " + clearedCount + " elements from cash.");
        } else {
            log.debug(ResourceManager.class.getName(), 1, "clearCash: cash has only " + cash.size() + "elements (MIN_ELEMENTS = " + MIN_ELEMENTS_CASH_COUNT_TO_CLEARING + "), than been full-cleared.");
            cash.clear();
        }
    }

    public synchronized static void add(Object index, URL fileURL, Boolean cashed) throws Exception {
        String name = String.valueOf(index);
        log.debug("Adding the " + name);

        resourseLinksMap.put(name, new File(fileURL.toURI()));
        if (cashed) {
            cash.put(name, Files.readAllBytes(Paths.get(fileURL.toURI())));
        }

        memoryControl();
    }

    public synchronized static void add(Object index, File file, Boolean cashed) throws Exception {
        String name = String.valueOf(index);
        log.debug("Adding the " + name);

        resourseLinksMap.put(name, file);
        if (cashed) {
            cash.put(name, Files.readAllBytes(file.toPath()));
        }
        imageBuffer.remove(name);

        memoryControl();
    }

    public synchronized static void reloadBytesFor(Object[] index, File[] files) throws Exception {
        log.debug("Reloading the cash... ");
        cash.clear();

        for (int f = 0; f < files.length; f++) {
            String name = String.valueOf(index[f]);
            cash.put(name, Files.readAllBytes(files[f].toPath()));
        }
    }


    public synchronized static AudioInputStream getAudioStream(Object index) throws Exception {
        String name = String.valueOf(index);
        log.debug("Getting the cashed AIS of " + name);

        if (cash.containsKey(name)) {
            return AudioSystem.getAudioInputStream(new ByteArrayInputStream(cash.get(name)));
        } else {
            if (resourseLinksMap.containsKey(name)) {
                add(name, resourseLinksMap.get(name), true);
                return AudioSystem.getAudioInputStream(new ByteArrayInputStream(cash.get(name)));
            }
        }

        return null;
    }


    public synchronized static BufferedImage getBufferedImage(Object index) throws Exception {
        return getBufferedImage(index, true, GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
    }

    public synchronized static BufferedImage getBufferedImage(Object index, Boolean transparensy) throws Exception {
        return getBufferedImage(index, true, GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
    }

    public synchronized static BufferedImage getBufferedImage(Object index, Boolean transparensy, GraphicsConfiguration gconf) throws Exception {
        // OPAQUE = 1; BITMASK = 2; TRANSLUCENT = 3;
        int transparensyInt = 3;

        String name = String.valueOf(index);
        ImageIO.setUseCache(false);
        BufferedImage tmp;
        log.debug("Getting the cashed BufferedImage of " + name);

        /*
         * Если в буфере уже есть имейдж - возвращаем его.
         * Иначе, заливаем и удаляем из кеша.
         *
         *  Т.к. изображение должно быть ИЛИ в кэше,
         *  ИЛИ в буфере. Не одновременно, для экономии памяти.
         */
        if (imageBuffer.containsKey(name)) {
            return imageBuffer.get(name);
        } else {
            if (cash.containsKey(name)) {
                // если в кэше есть файл, который нужно трансформировать в имейдж:
                tmp = gconf.createCompatibleImage(
                        ImageIO.read(new ByteArrayInputStream(cash.get(name))).getWidth(),
                        ImageIO.read(new ByteArrayInputStream(cash.get(name))).getHeight(),
                        transparensyInt
                );

                if (transparensy) {
                    tmp.getGraphics().drawImage(ImageIO.read(new ByteArrayInputStream(cash.get(name))), 0, 0, null);
                    imageBuffer.put(name, tmp);
                } else {
                    imageBuffer.put(name, ImageIO.read(new ByteArrayInputStream(cash.get(name))));
                }

                cash.remove(name);
            } else {
                if (resourseLinksMap.containsKey(name)) {
                    // если в кэше его нет, но есть ссылка на файл, который нужно прочитать как имейдж:
                    tmp = gconf.createCompatibleImage(
                            ImageIO.read(resourseLinksMap.get(name)).getWidth(),
                            ImageIO.read(resourseLinksMap.get(name)).getHeight(),
                            transparensyInt
                    );

                    if (transparensy) {
                        tmp.getGraphics().drawImage(ImageIO.read(resourseLinksMap.get(name)), 0, 0, null);
                        imageBuffer.put(name, tmp);
                    } else {
                        imageBuffer.put(name, ImageIO.read(resourseLinksMap.get(name)));
                    }
                } else {
                    // Ничего не найдено. Не зарегистрировано:
                    log.debug("BufferedImage '" + name + "' not exist into ResourceManager!");
                    return null;
                }
            }
        }

        return imageBuffer.get(name);
    }


    public synchronized static Cursor getCursor(Object index) throws Exception {
        return getCursor(String.valueOf(index), new Point(0, 0));
    }

    public synchronized static Cursor getCursor(Object index, Point dot) throws Exception {
        String name = String.valueOf(index);
        if (cash.containsKey(name)) {
            return toolkit.createCustomCursor(toolkit.createImage(cash.get(name)), dot, name);
        } else if (resourseLinksMap.containsKey(name)) {
            add(name, resourseLinksMap.get(name), true);
            return toolkit.createCustomCursor(toolkit.createImage(cash.get(name)), dot, name);
        }

        return null;
    }


    public synchronized static byte[] getBytes(Object index) throws Exception {
        String name = String.valueOf(index);
        if (cash.containsKey(name)) {
            return cash.get(name);
        } else if (resourseLinksMap.containsKey(name)) {
            add(name, resourseLinksMap.get(name), true);
            return cash.get(name);
        } else {
            System.out.println("File with name '" + name + "' dont exist into resourseBytesMap. Sorry!");
            return null;
        }
    }

    public synchronized static File getFilesLink(Object index) {
        String name = String.valueOf(index);
        try {
            return resourseLinksMap.get(name);
        } catch (Exception e) {
            if (resourseLinksMap.isEmpty()) {
                System.err.println("The LinksMap was cleaned already. It was You?..");
                e.printStackTrace();
            } else {
                System.err.println("The link of file '" + name + "' dont exist into LinksMap. Sorry!");
                e.printStackTrace();
            }
        }

        return null;
    }

    public synchronized static Set<Map.Entry<String, File>> getEntrySet() {
        return resourseLinksMap.entrySet();
    }

    public synchronized static Set<String> getKeySet() {
        return resourseLinksMap.keySet();
    }

    public synchronized static Collection<File> getValuesSet() {
        return resourseLinksMap.values();
    }


    public synchronized static void remove(Object index) {
        String name = String.valueOf(index);
        cash.remove(name);
        resourseLinksMap.remove(name);
    }

    public synchronized static void clearLinksMap() {
        resourseLinksMap.clear();
    }

    public synchronized static void clearAll() {
        cash.clear();
        resourseLinksMap.clear();
    }


    public synchronized static int getCashSize() {
        return cash.size();
    }

    public synchronized static int getLinksSize() {
        return resourseLinksMap.size();
    }

    public synchronized static long getCashVolume() {
        long bytesMapSize = 0L;

        for (int i = 0; i < cash.size(); i++) {
            bytesMapSize += cash.get(String.valueOf(i)).length;
        }

        return bytesMapSize;
    }
}
