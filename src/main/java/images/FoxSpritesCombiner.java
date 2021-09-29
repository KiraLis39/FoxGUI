package images;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;


public class FoxSpritesCombiner {
	private final static Map<String, BufferedImage[]> sptitesMap = new LinkedHashMap<String, BufferedImage[]>();
		
	private FoxSpritesCombiner() {}
	
	public static BufferedImage[] addSpritelist(String spriteListName, BufferedImage image, int wCount, int hCount) {
		BufferedImage[] result = null;
		
		if (spriteListName.equals("") || image == null || wCount <= 0 || hCount <= 0) {
			throw new RuntimeException("fox.games.SpriteCombine.addNewSpritelist(): Not correct income params: " 
								+ spriteListName + " as Name, " 	+ image.toString() + " as ImageIcon, "
								+ wCount + "x" + hCount + " as W and H resolutions.");
		} else {
			if (!sptitesMap.containsKey(spriteListName)) {
				try{sptitesMap.put(spriteListName, result = loadAndCut(image, wCount, hCount));} catch (Exception e) {e.printStackTrace();}
			} else {
				log("The key '" + spriteListName + "' exist into map already.");
				return getSprites(spriteListName);
			}
		}
		
		return result;
	}

	private static BufferedImage[] loadAndCut(BufferedImage image, int w, int h) {
		BufferedImage[] result = null;

		int spriteWidth = image.getWidth() / w;
		int spriteHeight = image.getHeight() / h;
		result = new BufferedImage[w * h];
//		Out.Print("fox.games.FoxSpritesCombiner.loadAndCut():", 0, "Нарезка " + (w * h) + " спрайтов...");
		
		int i = 0;
		Graphics2D g2D = null;
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				result[i] = new BufferedImage(spriteWidth, spriteHeight, BufferedImage.TYPE_INT_ARGB);
				g2D = result[i].createGraphics();
//				setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.0f));
				render(g2D);
				
				g2D.drawImage(image, 
						0, 	0, 
						result[i].getWidth(), 	result[i].getHeight(), 

						spriteWidth * x, 			spriteHeight * y, 
						spriteWidth * (x + 1), 	spriteHeight * (y + 1), 
						
						null);
				i++;
			}
		}
		
		g2D.dispose();
		
//		Out.Print("fox.games.FoxSpritesCombiner.loadAndCut():", 0, "Спрайтов готово: " + result.length);
//		Out.Print("fox.games.FoxSpritesCombiner.loadAndCut():", 0, "Размер спрайта: " + result[0].getWidth() + "x" + result[0].getHeight() + "px");
		
		return result;
	}

	public static BufferedImage[] getSprites(String spriteName) {
		if (sptitesMap == null) {throw new RuntimeException("FoxSpritesCombiner: getSprites(): This map is NULL. Would You create one?");}
		if (sptitesMap.get(spriteName) == null) {throw new RuntimeException("fox.games.FoxSpritesCombiner: getSprites(): This map.get(spriteName) is NULL. Its not good.");}

		return sptitesMap.get(spriteName);
	}

	public static void removeSprite(String spriteName) {
		if (sptitesMap == null) {throw new RuntimeException("FoxSpritesCombiner: removeSprite(): This map is NULL. Would You create one?");}
		if (sptitesMap.get(spriteName) == null) {
			log("removeSprite(): Sprite " + spriteName + " is not exist here.");
			return;
		}
		
		sptitesMap.remove(spriteName);
	}
	
	private static void render(Graphics2D g2D) {
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
		g2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	}
	
	private static void log(String message) {
		System.out.println(FoxSpritesCombiner.class.getName() + " : " + message);
	}
}