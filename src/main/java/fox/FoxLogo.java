package fox;

import fox.utils.InputAction;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

@Data
@Slf4j
@RequiredArgsConstructor
public class FoxLogo implements Runnable {
    private final FoxRender foxRender = new FoxRender();
    private final InputAction inputAction = new InputAction();
    private long timeStamp;
    private Font customFont;
    private Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    private IMAGE_STYLE imStyle = IMAGE_STYLE.DEFAULT;
    private BACK_STYLE bStyle = BACK_STYLE.ASIS;
    private JFrame logoFrame;
    private Thread engine;
    private BufferedImage[] images;
    private Color color;
    private Color logoBackColor;
    private Raster raster;
    private int breakKey = KeyEvent.VK_ESCAPE;
    private int picCounter = -1;
    private int fps = 30;
    private int imageShowTime = 5000;
    private float alphaGrad = 0f;
    private boolean isBroken = false;
    private boolean highQualityMode = false;

    public void start(String cornerLabelText, BufferedImage... textureFilesMassive) {
        start(cornerLabelText, imStyle, bStyle, breakKey, textureFilesMassive);
    }

    public void start(String cornerLabelText, IMAGE_STYLE imStyle, BACK_STYLE bStyle, BufferedImage... textureFilesMassive) {
        start(cornerLabelText, imStyle, bStyle, breakKey, textureFilesMassive);
    }

    public void start(String cornerLabelText, IMAGE_STYLE imStyle, BACK_STYLE bStyle, int breakKey, BufferedImage... textureFilesMassive) {
        if (textureFilesMassive == null || textureFilesMassive.length == 0) {
            log.error("Textures massive is can not be a NULL or empty");
            throw new NoSuchElementException("Textures massive is NULL or empty");
        }
        log.debug("Load StartLogo`s images count: {}", textureFilesMassive.length);
        images = textureFilesMassive;

        log.info("Set the StartLogo`s break key to {}", KeyEvent.getKeyText(breakKey));
        this.breakKey = breakKey;

        this.imStyle = imStyle;
        this.bStyle = bStyle;

        logoFrame = new LogoFrame(cornerLabelText);

        engine = new Thread(this);
        engine.start();
    }

    @Override
    public void run() {
        loadNextImage();
        logoFrame.setVisible(true);
        timeStamp = System.currentTimeMillis();
        while (!isBroken && !engine.isInterrupted()) {
            try {
                logoFrame.repaint();
                TimeUnit.MILLISECONDS.sleep(1000 / fps);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        finalLogo();
        logoFrame.dispose();
    }

    private void loadNextImage() {
        picCounter++;
        if (picCounter >= images.length) {
            isBroken = true;
        } else {
            timeStamp = System.currentTimeMillis();
            raster = images[picCounter].getRaster();
            Object data = raster.getDataElements(1, images[picCounter].getHeight() / 2, null);
            logoBackColor = new Color(images[picCounter].getColorModel().getRGB(data), true);
            alphaGrad = 0;
        }
    }

    public void finalLogo() {
        isBroken = true;
    }

    public void join() throws InterruptedException {
        if (engine != null && engine.isAlive()) {
            engine.join();
        }
    }

    public int getLogoFps() {
        return fps;
    }

    /**
     * FPS или количество кадров на секунду, которое лого будет перерисовываться при анимации на экране.
     * По умолчанию значение - 30
     *
     * @param fps новое значение кадров в секунду.
     */
    public void setLogoFps(int fps) {
        this.fps = fps;
    }

    public int getImageShowTime() {
        return imageShowTime;
    }

    /**
     * Время "зависания" лого после появления на экране. Перед тем, как начнется анимация завершения (если она включена).
     *
     * @param imageShowTime новое время ожидания на экране (по умолчанию - 5_000 мс)
     */
    public void setImageShowTime(int imageShowTime) {
        this.imageShowTime = imageShowTime;
    }

    public enum IMAGE_STYLE {FILL, DEFAULT, WRAP}

    public enum BACK_STYLE {ASIS, PICK, COLOR}

    private class LogoFrame extends JFrame {
        private final String cornerLabelText;
        private boolean isLogoAppearing = true, isLogoVanishing = false;

        public LogoFrame(String cornerLabelText) throws HeadlessException {
            this.cornerLabelText = cornerLabelText;

            setFocusable(true);
            setUndecorated(true);
            setBackground(new Color(0, 0, 0, 0));
            setExtendedState(Frame.MAXIMIZED_BOTH);

            inAc(this);

            setLocationRelativeTo(null);
        }

        @Override
        public void paint(Graphics g) {
            if (isBroken) {
                return;
            }
            super.paint(g);

            Graphics2D g2D = (Graphics2D) g;
            foxRender.setRender(g2D, FoxRender.RENDER.MED);

            if (isLogoAppearing) gradeUp();

            if (bStyle == BACK_STYLE.ASIS) {
                g2D.setColor(logoBackColor);
                g2D.fillRect(0, 0, getWidth(), getHeight());
            } else if (bStyle == BACK_STYLE.COLOR) {
                g2D.setColor(color == null ? Color.MAGENTA : color);
                g2D.fillRect(0, 0, getWidth(), getHeight());
            }

            g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaGrad));

            float imageWidth, imageHeight;
            if (imStyle == IMAGE_STYLE.WRAP) { // style is WRAP:
                imageWidth = images[picCounter].getWidth();
                imageHeight = images[picCounter].getHeight();
                while (imageWidth > screen.width) {
                    imageWidth -= 4;
                    imageHeight -= 2;
                }
                while (imageHeight > screen.height) {
                    imageHeight -= 4;
                    imageWidth -= 2.5f;
                }

            } else if (imStyle == IMAGE_STYLE.FILL) { // style is FILL:
                imageWidth = (float) screen.getWidth();
                imageHeight = (float) screen.getHeight();

            } else { // style is DEFAULT:
                imageWidth = images[picCounter].getWidth();
                imageHeight = images[picCounter].getHeight();
            }

            drawImage(g2D, (int) imageWidth, (int) imageHeight);
            drawText(g2D);

            g2D.dispose();

            if (System.currentTimeMillis() - timeStamp > imageShowTime && isLogoVanishing) {
                gradeDown();
            }
            if (alphaGrad == 0) {
                loadNextImage();
            }
        }

        private void drawText(Graphics2D g2D) {
            if (cornerLabelText != null && !cornerLabelText.isBlank()) {
                g2D.setColor(Color.BLACK);
                if (customFont != null) {
                    g2D.setFont(customFont);
                }
                g2D.drawString(cornerLabelText, 30, 30);
            }
        }

        private void drawImage(Graphics2D g2D, int imWidth, int imHeight) {
            if (images.length > picCounter) {
                g2D.drawImage(images[picCounter],
                        screen.width / 2 - imWidth / 2,
                        screen.height / 2 - imHeight / 2,
                        imWidth, imHeight, logoFrame);
            }
        }

        private void gradeUp() {
            if (alphaGrad > 0.94f) {
                alphaGrad = 1f;
                isLogoAppearing = false;
                isLogoVanishing = true;
            } else {
                alphaGrad += 0.05f;
            }
        }

        private void gradeDown() {
            if (alphaGrad < 0.076f) {
                alphaGrad = 0f;
                isLogoAppearing = true;
                isLogoVanishing = false;
            } else {
                alphaGrad -= 0.075f;
            }
        }

        private void inAc(JFrame logo) {
            inputAction.add("logoFrame", logo.getRootPane());
            inputAction.set(JComponent.WHEN_FOCUSED, "logoFrame", "final", breakKey, 0, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    finalLogo();
                }
            });
        }
    }
}
