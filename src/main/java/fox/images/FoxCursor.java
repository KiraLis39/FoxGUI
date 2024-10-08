package fox.images;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
public final class FoxCursor {
    private FoxCursor() {
    }

    public static Cursor createCursor(@NonNull Icon ico, String curName) {
        try {
            BufferedImage tmp = new BufferedImage(ico.getIconWidth(), ico.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) tmp.getGraphics();
            g2d.setStroke(new BasicStroke(1));
            g2d.setColor(Color.WHITE);
            g2d.fillOval(0, 0, tmp.getWidth(), tmp.getHeight());
            g2d.setColor(Color.YELLOW);
            g2d.fillRect(0, 0, 16, 16);
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawOval(0, 0, tmp.getWidth(), tmp.getHeight());
            ico.paintIcon(null, g2d, 0, 0);
            g2d.dispose();

            return createCursor(tmp, curName);
        } catch (Exception e) {
            log.error("Ошибка создания курсора: {}", e.getMessage());
        }

        return null;
    }

    public static Cursor createCursor(@NonNull Path imagePath) throws IOException {
        BufferedImage ico = ImageIO.read(imagePath.toFile());
        if (ico == null) {
            System.err.println("fox.games.FoxCursors (getCursor): Cursor " + imagePath + " has not found!");
            return null;
        }

        return createCursor(ico, imagePath.getFileName().toString());
    }

    public static Cursor createCursor(@NonNull BufferedImage bImage, String name) {
        try {
            return Toolkit.getDefaultToolkit().createCustomCursor(bImage, new Point(0, 0), name);
        } catch (Exception e) {
            log.error("Ошибка создания курсора: {}", e.getMessage());
        }
        return null;
    }

    public static Cursor createCursor(@NonNull ImageIcon iImage, String name) {
        try {
            return Toolkit.getDefaultToolkit().createCustomCursor(iImage.getImage(), new Point(0, 0), name);
        } catch (Exception e) {
            log.error("Ошибка создания курсора: {}", e.getMessage());
        }
        return null;
    }
}
