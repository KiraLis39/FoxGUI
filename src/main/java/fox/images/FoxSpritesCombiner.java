package fox.images;

import lombok.NonNull;

import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FoxSpritesCombiner {
    private final Map<String, BufferedImage[]> spritesMap = new LinkedHashMap<>();

    public BufferedImage[] getSprites(@NonNull String spriteName, @NonNull BufferedImage bImage, int rows, int columns) {
        if (spritesMap.containsKey(spriteName)) {
            return spritesMap.get(spriteName);
        }

        if (bImage.getHeight() <= 1 || bImage.getHeight() <= 1) {
            throw new RuntimeException("Width or height of the bImage is too small!");
        }

        int iter = 0;
        double tileWidth = (double) bImage.getWidth() / columns;
        double tileHeight = (double) bImage.getHeight() / rows;
        int tilesCountExpected = rows * columns;
        BufferedImage[] result = new BufferedImage[tilesCountExpected];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                result[iter] = bImage.getSubimage(
                        (int) tileWidth * col, (int) tileHeight * row,
                        (int) tileWidth, (int) tileHeight);
                iter++;
            }
        }

        spritesMap.put(spriteName, result);
        return result;
    }

    public void removeSprites(@NonNull String spriteName) {
        if (!spritesMap.containsKey(spriteName)) {
            return;
        }
        spritesMap.remove(spriteName);
    }
}
