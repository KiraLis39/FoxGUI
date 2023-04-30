package fox;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

@Slf4j
public class FoxFontBuilder {
    private static final FONT defaultFont = FONT.ARIAL_NARROW;
    private final GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
    private final List<String> fArr = new LinkedList<>(); // набор шрифтов по-умолчанию.
    private Path fontsDirectory; // папка с дополнительными шрифтами TRUETYPE

    public FoxFontBuilder() {
        for (FONT value : FONT.values()) {
            fArr.add(value.getValue());
        }
    }

    // выбор шрифта:
    public Font setFoxFont(FONT fontName, float fontSize, Boolean isBold) {
        return setFoxFont(fontName.ordinal(), Math.round(fontSize), isBold, gEnv);
    }

    public Font setFoxFont(FONT fontName, float fontSize, Boolean isBold, GraphicsEnvironment gEnv) {
        return setFoxFont(fontName.ordinal(), Math.round(fontSize), isBold, gEnv);
    }

    public Font setFoxFont(int id, int fontSize, boolean isBold, GraphicsEnvironment gEnv) {
        if (id > fArr.size() - 1) {
            fontNotExistsMessage(id);
            return new Font(fArr.get(defaultFont.ordinal()), isBold ? Font.BOLD : Font.PLAIN, fontSize); // BOLD, ITALIC, BOLD+ITALIC
        }

        if (!isFontExist(id, gEnv)) {
            if (fontsDirectory == null) {
                fontsDirectory = Paths.get("./fonts/");
                try {
                    if (Files.notExists(fontsDirectory)) {
                        Files.createDirectory(fontsDirectory);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // если в ОС нет шрифта, но указана папка c ним:
                try {
                    log.debug("Now will be setup fonts...");
                    for (File font : Objects.requireNonNull(fontsDirectory.toFile().listFiles())) {
                        try {
                            register(Font.createFont(Font.TRUETYPE_FONT, font), gEnv);
                        } catch (Exception e) {
                            log.warn("Не удалось подключить шрифт " + font.getName() + " как TRUETYPE." + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Error with font existing! Set fonts dir by methode:  setFontsDirectory(File fontsDirectory) where fontsDirectory is a folder with fonts from FoxLib jar archive.fonts");
                    if (!Files.notExists(fontsDirectory)) {
                        log.warn("FAILED!");
                    } else {
                        log.debug("Success!");
                    }
                }
            }

            // если не получилось, возвращаем шрифт по-умолчанию:
            if (!isFontExist(id, gEnv)) {
                return new Font(fArr.get(defaultFont.ordinal()), isBold ? Font.BOLD : Font.PLAIN, fontSize);
            }
        }

        return new Font(fArr.get(id), isBold ? Font.BOLD : Font.PLAIN, fontSize);
    }

    private boolean isFontExist(int ID, GraphicsEnvironment gEnv) {
        for (String fname : gEnv.getAvailableFontFamilyNames()) {
            if (fname.equalsIgnoreCase(fArr.get(ID))) {
                return true;
            }
        }

        log.debug("Font '" + fArr.get(ID) + "' not exists in this OS! Please setup it if you can.");
        return false;
    }

    public Double getStringCenterX(Graphics gr, String string) {
        return getStringBounds(gr, string).getCenterX();
    }

    public Double getStringCenterY(Graphics gr, String string) {
        return getStringBounds(gr, string).getCenterY();
    }

    public Rectangle2D getStringBounds(Graphics gr, String string) {
        return gr.getFontMetrics(gr.getFont()).getStringBounds(string, gr);
    }

    public int addNewFont(String newFontName) {
        fArr.add(newFontName);
        return getFontID(newFontName);
    }

    public int getFontID(String fontName) {
        for (int i = 0; i < fArr.size(); i++) {
            if (fArr.get(i).equals(fontName)) {
                return i;
            }
        }

        return -1;
    }

    public int getFontArraySize() {
        return fArr.size();
    }

    public Set<Entry<Integer, String>> getAllFontsTable() {
        Map<Integer, String> tmpMap = new LinkedHashMap<>();

        for (int fontCount = 0; fontCount < fArr.size(); fontCount++) {
            tmpMap.put(fontCount, fArr.get(fontCount));
        }

        if (!tmpMap.isEmpty()) {
            return tmpMap.entrySet();
        } else {
            return Collections.emptySet();
        }
    }

    private void fontNotExistsMessage(int ID) {
        JOptionPane.showMessageDialog(null,
                "<html>В FoxFontBuilder нет шрифта с ID " + ID + ".<br>"
                        + "Воспользуйтесь методами для получения количества доступных<br>"
                        + "или методами добавления своего шрифта.",
                "Ошибка!", JOptionPane.WARNING_MESSAGE);
    }

    public Path getFontsDirectory() {
        return fontsDirectory;
    }

    public void setFontsDirectory(Path _fontsDirectory) {
        if (Files.notExists(Paths.get("./fonts/"))) {
            log.debug("The fonts path is not exist! (" + _fontsDirectory + ").");
        }
        fontsDirectory = _fontsDirectory;
    }

    public Font[] getAvailableFonts() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    }

    public Font getAvailableFontByIndex(int index) {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()[index];
    }

    /**
     * @return index of registered font
     */
    public int register(Font f, GraphicsEnvironment gEnv) throws Exception {
        if (gEnv.registerFont(f)) {
            fArr.add(f.getFontName());
            return fArr.indexOf(f.getFontName());
        } else {
            Optional<Font> found = Arrays.stream(gEnv.getAllFonts())
                    .filter(fnt -> fnt.getFontName().equalsIgnoreCase(f.getFontName())).findAny();
            if (found.isPresent()) {
                log.debug("FoxFontBuilder.register: The font " + f + " is registered already.");
                fArr.add(f.getFontName());
                return fArr.indexOf(f.getFontName());
            }
            throw new Exception("FoxFontBuilder.register: Can`t register the font " + f);
        }

//		InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream("roboto-bold.ttf");
//		Font font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(48f);
//		Font font = Font.createFont(Font.TRUETYPE_FONT, new File("Fonts\\custom_font.ttf")).deriveFont(12f);
    }

    public enum FONT {
        COMIC_SANS("Comic Sans MS"),
        MONOTYPE_CORSIVA("Monotype Corsiva"),
        BAHNSCHRIFT("bahnschrift"),
        CANDARA("Candara"),
        HARLOW_S_I("Harlow Solid Italic"),
        CORBEL("Corbel"),
        GEORGIA("Georgia"),
        ARIAL("Arial"),
        ARIAL_NARROW("Arial Narrow"),
        SEGOE_SCRIPT("Segoe Script"),
        CAMBRIA("Cambria"),
        CONSTANTIA("Constantia"),
        CONSOLAS("Consolas"),
        PAPYRYS("Papyrus"),
        LEELAWADEE("Leelawadee UI"),
        SEGOE_UI_SYMBOL("Segoe UI Symbol"),
        TIMES_NEW_ROMAN("Times New Roman"),
        COURIER_NEW("Courier New");

        final String value;

        FONT(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
