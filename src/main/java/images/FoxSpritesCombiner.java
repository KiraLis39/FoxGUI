package images;

import lombok.NonNull;
import render.FoxRender;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

public class FoxSpritesCombiner {
	private final static Map<String, BufferedImage[]> spritesMap = new LinkedHashMap<>();
		
	private FoxSpritesCombiner() {}

//	@Deprecated
//	public static BufferedImage[] addSpritelist(@NonNull String spriteName, @NonNull BufferedImage bImage, int columns, int rows) {
//		BufferedImage[] result = null;
//
//		if (spriteName.isBlank() || bImage.getWidth() <= 1 || bImage.getHeight() <= 1 || (columns <= 1 && rows <= 1)) {
//			throw new RuntimeException("FoxSpritesCombiner.addSpritelist: Not correct income params: '"
//								+ spriteName + "' as Name, '" 	+ bImage + "' as Image, '"
//								+ columns + "x" + rows + "' as columns and rows.");
//		} else {
//			if (spritesMap.containsKey(spriteName)) {
//				return spritesMap.get(spriteName);
//			} else {
//				try{spritesMap.put(spriteName, result = loadAndCut(bImage, columns, rows));
//				} catch (Exception e) {e.printStackTrace();}
//			}
//		}
//
//		return result;
//	}

//	@Deprecated
//	private static BufferedImage[] loadAndCut(BufferedImage image, int w, int h) {
//		BufferedImage[] result = null;
//
//		int spriteWidth = image.getWidth() / w;
//		int spriteHeight = image.getHeight() / h;
//		result = new BufferedImage[w * h];
//
//		int i = 0;
//		Graphics2D g2D = null;
//
//		for (int y = 0; y < h; y++) {
//			for (int x = 0; x < w; x++) {
//				result[i] = new BufferedImage(spriteWidth, spriteHeight, BufferedImage.TYPE_INT_ARGB);
//				g2D = result[i].createGraphics();
//				FoxRender.setRender(g2D, FoxRender.RENDER.HIGH);
//
//				g2D.drawImage(image,
//						0, 	0,
//						result[i].getWidth(), result[i].getHeight(),
//						spriteWidth * x,spriteHeight * y,
//						spriteWidth * (x + 1),spriteHeight * (y + 1),
//						null);
//				i++;
//			}
//		}
//
//		g2D.dispose();
//
//		return result;
//	}

	public static BufferedImage[] getSprites(@NonNull String spriteName, @NonNull BufferedImage bImage, int rows, int columns) {
		if (spritesMap.containsKey(spriteName)) {
			return spritesMap.get(spriteName);
		}

		if (bImage.getHeight() <= 1 || bImage.getHeight() <= 1) {
			throw new RuntimeException("Width or height of the bImage is too small!");
		}

		int iter = 0;
		Double tileWidth = bImage.getWidth() / columns * 1d;
		Double tileHeight = bImage.getHeight() / rows * 1d;
		int tilesCountExpected = rows * columns;
		BufferedImage[] result = new BufferedImage[tilesCountExpected];
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < columns; col++) {
				result[iter] = bImage.getSubimage(
						tileWidth.intValue() * col, tileHeight.intValue() * row,
						tileWidth.intValue(), tileHeight.intValue());
				iter++;
			}
		}

		spritesMap.put(spriteName, result);
		return result;
	}

	public static void removeSprites(@NonNull String spriteName) {
		if (!spritesMap.containsKey(spriteName)) {return;}
		spritesMap.remove(spriteName);
	}
}
