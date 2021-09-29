package fox;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import javax.imageio.ImageIO;

import render.FoxRender;


public class ResManager {
	private final static Map<String, BufferedImage> imageBuffer = Collections.synchronizedMap(new LinkedHashMap<String, BufferedImage> ());
	private final static Map<String, File> resourseLinksMap = Collections.synchronizedMap(new LinkedHashMap<String, File> ());
	private final static Map<String, byte[]> cash = Collections.synchronizedMap(new LinkedHashMap<String, byte[]> ());
	
	private final static long MAX_MEMORY = Runtime.getRuntime().maxMemory() - 1L;
	private static long USED_MEMORY, MAX_LOADING;
	
	private static int HQ = 0, MIN_ELEMENTS_CASH_COUNT_TO_CLEARING = 128, MIN_ELEMENTS_BIMAGE_COUNT_TO_CLEARING = 64;
	
	private static float memGCTrigger = 0.75f;
	
	private static boolean logEnable = true;
	
	
	private ResManager() {}
	
	
	// опасно заливать память тоннами мусора. Не бойся, мемориКонтроль спасёт тебя =^_^=:
	public static void memoryControl() {
		USED_MEMORY = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
		MAX_LOADING = (long) (MAX_MEMORY * memGCTrigger);
		
		if (USED_MEMORY > MAX_LOADING) {
			log("ResourceManager: Memory control (USED " + (USED_MEMORY / 1048576L) + " > " + ((int) (memGCTrigger * 100)) + "% from MAX " + (MAX_MEMORY / 1048576L) + ")\nClearing...");
			
			try {
				int clearedCount = 0;
				if (cash.size() > MIN_ELEMENTS_CASH_COUNT_TO_CLEARING) {
					for (Entry<String, byte[]> entry : cash.entrySet()) {
						if (entry.getValue().length > MAX_LOADING * 0.05f) { // если кэшированная штука занимает больше 5% разрешенной памяти.
							cash.remove(entry.getKey());
							clearedCount++;
						}
					}
					
					log("clearCash: was removed " + clearedCount + " elements from cash.");
				} else {
					log("clearCash: cash has only " + cash.size() + "elements (MIN_ELEMENTS = " + MIN_ELEMENTS_CASH_COUNT_TO_CLEARING + "), than been full-cleared.");
					cash.clear(); // если навыбирать ничего не вышло - освобождать что-то же нужно..
				}
				
				clearedCount = 0;
				if (imageBuffer.size() > MIN_ELEMENTS_BIMAGE_COUNT_TO_CLEARING) {
					for (Entry<String, BufferedImage> entry : imageBuffer.entrySet()) {
						if (entry.getValue().getData().getDataBuffer().getSize() * 4L > MAX_LOADING * 0.1f) { // если кэшированная картинка занимает больше 10% разрешенной памяти.
							imageBuffer.remove(entry.getKey());
							clearedCount++;
						}
					}
					
					log("clearBImage: was removed " + clearedCount + " pictures from buffer.");
				} else {
					log("clearBImage: cash has only " + imageBuffer.size() + "elements (MIN_ELEMENTS = " + MIN_ELEMENTS_BIMAGE_COUNT_TO_CLEARING + "), than been full-cleared.");
					imageBuffer.clear(); // если навыбирать ничего не вышло - освобождать что-то же нужно..
				}
			} catch (Exception e) {
				log("Was catched overlap! Hooray! using > " + (USED_MEMORY / 1048576L) + " / " + (MAX_MEMORY / 1048576L) + " >> " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	// заливаем новый ресурс:
	public synchronized static void add(Object index, File file) throws Exception {add(index, file, false);}	
	public synchronized static void add(Object index, BufferedImage bImage) throws Exception {add(index, bImage, false);}	
	public synchronized static void add(Object index, URL fileURL) throws Exception {add(index, fileURL, false);}	
	public synchronized static void add(Object index, URL fileURL, Boolean isImage) throws Exception {add(index, new File(fileURL.getFile()), isImage);}		
	public synchronized static void add(Object index, Object file, Boolean isImage) throws Exception {
		if (isImage == null) {isImage = false;}
		if (file == null || Files.notExists(Paths.get(((File) file).toURI()))) {throw new RuntimeException("Object file cant be a NULL and should exist!");}
		log("Try to add the resource '" + index + "'...");
		
		String name = String.valueOf(index);
		if (file instanceof BufferedImage) {
			imageBuffer.put(name, (BufferedImage) file);
			return;
		} else if (file instanceof File) {		
			resourseLinksMap.put(name, (File) file);
			if (!isImage) {cash.put(name, Files.readAllBytes(((File) file).toPath()));
			} else {imageBuffer.put(name, ImageIO.read((File) file));}
		}
		
		memoryControl();
	}
	// удаляем ресурс:
	public synchronized static void remove(Object index) {
		String name = String.valueOf(index);
		if (cash.containsKey(name)) {cash.remove(name);}
		if (imageBuffer.containsKey(name)) {imageBuffer.remove(name);}
		if (resourseLinksMap.containsKey(name)) {resourseLinksMap.remove(name);}
	}

	// забираем картинку:
	public synchronized static BufferedImage getBImage(Object index) {return getBImage(index, true);}	
	public synchronized static BufferedImage getBImage(Object index, boolean transparensy) {
		return getBImage(index, transparensy, GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
	}
	public synchronized static BufferedImage getBImage(Object index, boolean transparensy, GraphicsConfiguration gconf) {
		if (gconf == null) {gconf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();}
		
		Objects.requireNonNull(index);		
		String name = String.valueOf(index);
		if (name.isEmpty() || name.isBlank()) {
			log("Index of image is empty?");
			return null;
		}
		
		log("Getting the cashed BufferedImage of " + name);
		
		if (imageBuffer.containsKey(name)) {return imageBuffer.get(name);
		} else {
			ImageIO.setUseCache(false);
			HQ = 3; // OPAQUE = 1; BITMASK = 2; TRANSLUCENT = 3;
						
			if (cash.containsKey(name)) {buildImageByCash(name, transparensy, gconf);
			} else {
				if (resourseLinksMap.containsKey(name)) {buildImageByLink(name, transparensy, gconf);
				} else {
					log("BufferedImage '" + name + "' not exist into ResourceManager!");
					return null;
				}
			}
			
			return imageBuffer.get(name);
		}
	}
	
	
	private static void buildImageByLink(String name, boolean transparensy, GraphicsConfiguration gconf) {
		try {
			BufferedImage tmp = gconf.createCompatibleImage(
					ImageIO.read(resourseLinksMap.get(name)).getWidth(), 
					ImageIO.read(resourseLinksMap.get(name)).getHeight(),
					HQ
			);

			if (transparensy) {
				Graphics2D g2D = (Graphics2D) tmp.getGraphics();
				FoxRender.setHQRender(g2D);
				g2D.drawImage(ImageIO.read(resourseLinksMap.get(name)), 0, 0, null);
				g2D.dispose();
				
				imageBuffer.put(name, tmp);
			} else {imageBuffer.put(name, ImageIO.read(resourseLinksMap.get(name)));}
		} catch (Exception e) {
			log("Operation buildImageByLink has failed!");
			e.printStackTrace();
		}
	}

	private static void buildImageByCash(String name, boolean transparensy, GraphicsConfiguration gconf) {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(cash.get(name))) {
			BufferedImage imRb = ImageIO.read(bais);
			
			BufferedImage tmp = gconf.createCompatibleImage(imRb.getWidth(), imRb.getHeight(), HQ);				
			
			if (transparensy) {
				Graphics2D g2D = (Graphics2D) tmp.getGraphics();
				FoxRender.setHQRender(g2D);			
				g2D.drawImage(imRb, 0, 0, null);
				g2D.dispose();
				
				imageBuffer.put(name, tmp);
			} else {imageBuffer.put(name, imRb);}
			
			cash.remove(name);
		} catch (Exception e) {
			log("Operation buildImageByCash has failed!");
			e.printStackTrace();
		}
	}

	// получить ширину-высоту картинки до её получения отсюда:
	public synchronized static Dimension getBImageDim(Object index) {
		try {return new Dimension(imageBuffer.get(index.toString()).getWidth(), imageBuffer.get(index.toString()).getHeight());
		} catch (Exception e) {return null;}
	}
	
	// получить массив байтов ресурса:
	public synchronized static byte[] getBytes(Object index) {
		String name = String.valueOf(index);
		if (cash.containsKey(name)) {return cash.get(name);
		} else if (resourseLinksMap.containsKey(name)) {
			try {add(name, resourseLinksMap.get(name), true);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return cash.get(name);
		} else {
			log("RM.getBytes(): File with name '" + name + "' dont exist into BytesMap!");
			return null;
		}
	}
	
	// получить ссылку на ресурс:
	public synchronized static File getFileLink(Object index) {
		String name = String.valueOf(index);
		try {return resourseLinksMap.get(name);
		} catch (Exception e) {
			if (resourseLinksMap.isEmpty()) {log("The LinksMap was cleaned already. It was You?.. (" + e.getMessage() + ")");
			} else {log("The link of file '" + name + "' dont exist into LinksMap. Sorry! (" + e.getMessage() + ")");}
		}
		
		return null;
	}
	
	
	public synchronized static Set<String> getLinksKeys() {return resourseLinksMap.keySet();}
	public synchronized static Collection<File> getLinksValues() {return resourseLinksMap.values();}
	public synchronized static Set<Entry<String, File>> getLinksEntrySet() {return resourseLinksMap.entrySet();}	

	
	public synchronized static void clearImages() {imageBuffer.clear();}
	public synchronized static void clearAll() {
		cash.clear();
		imageBuffer.clear();
		resourseLinksMap.clear();
	}
	
	public synchronized static int getCashSize() {return cash.size();}
	public synchronized static int getImagesSize() {return imageBuffer.size();}
	public synchronized static int getLinksSize() {return resourseLinksMap.size();}
	
	public synchronized static long getCashVolume() {
		long bytesMapSize = 0L;		
		for (int i = 0; i < cash.size(); i++) {bytesMapSize += cash.get(String.valueOf(i)).length;}		
		return bytesMapSize;		
	}

	
	public static Boolean isDebugOn() {return logEnable;}
	public static void setDebugOn(Boolean enabled) {logEnable = enabled;}
	
	public static void setMemoryLoadFactor(float memGCTrigger) {
		ResManager.memGCTrigger = memGCTrigger;
	}
	
	private static void log(String message) {System.out.println(message);}
}