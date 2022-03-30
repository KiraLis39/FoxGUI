package utils;

import fox.Out;
import fox.Out.LEVEL;

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


public class FoxFontBuilder {
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
    private static final FONT defaultFont = FONT.ARIAL_NARROW;

    private static GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
    private static final List<String> fArr = new LinkedList<>(); // набор шрифтов по-умолчанию.
    private static Path fontsDirectory; // папка с дополнительными шрифтами TRUETYPE

    static {
        for (FONT value : FONT.values()) {
            fArr.add(value.getValue());
        }
    }

    private FoxFontBuilder(GraphicsEnvironment _gEnv) {
        gEnv = _gEnv;
    }


    // выбор шрифта:
    public static Font setFoxFont(FONT fontName, float fontSize, Boolean isBold) {
        return setFoxFont(fontName.ordinal(), Math.round(fontSize), isBold, gEnv);
    }

    public static Font setFoxFont(FONT fontName, float fontSize, Boolean isBold, GraphicsEnvironment gEnv) {
        return setFoxFont(fontName.ordinal(), Math.round(fontSize), isBold, gEnv);
    }

    public static Font setFoxFont(int ID, int fontSize, Boolean isBold, GraphicsEnvironment gEnv) {
        if (ID > fArr.size() - 1) {
            fontNotExistsMessage(ID);
            return new Font(fArr.get(defaultFont.ordinal()), isBold ? Font.BOLD : Font.PLAIN, fontSize); // BOLD, ITALIC, BOLD+ITALIC
        }

        if (!isFontExist(ID, gEnv)) {
            if (fontsDirectory == null) {
                fontsDirectory = Paths.get("./fonts/");
                try {
                    if (Files.notExists(fontsDirectory)) {
                        Files.createDirectory(fontsDirectory);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fontsDirectory != null) {
                // если в ОС нет шрифта, но указана папка c ним:
                try {
                    log("Now will be setup fonts...");
                    for (File font : fontsDirectory.toFile().listFiles()) {
                        try {
                            register(Font.createFont(Font.TRUETYPE_FONT, font), gEnv);
                        } catch (Exception e) {
                            log("Не удалось подключить шрифт " + font.getName() + " как TRUETYPE." + e.getMessage());
                            continue;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log("Error with font existing! Set fonts dir by methode:  setFontsDirectory(File fontsDirectory) where fontsDirectory is a folder with fonts from FoxLib jar archive.fonts");
                    if (!Files.notExists(fontsDirectory)) {
                        log("FAILED!");
                    } else {
                        log("Success!");
                    }
                }
            }

            // если не получилось, возвращаем шрифт по-умолчанию:
            if (!isFontExist(ID, gEnv)) {
                return new Font(fArr.get(defaultFont.ordinal()), isBold ? Font.BOLD : Font.PLAIN, fontSize);
            }
        }

        return new Font(fArr.get(ID), isBold ? Font.BOLD : Font.PLAIN, fontSize);
    }

    private static boolean isFontExist(int ID, GraphicsEnvironment gEnv) {
        for (String fname : gEnv.getAvailableFontFamilyNames()) {
            if (fname.equalsIgnoreCase(fArr.get(ID))) {
                return true;
            }
        }

        log("Font '" + fArr.get(ID) + "' not exists in this OS! Please setup it if you can.");
        return false;
    }

//    public static Double getStringHeight(Graphics gr, String string) {
//        return getStringBounds(gr, string).getHeight();
//    }
//
//    public static Double getStringWidth(Graphics gr, String string) {
//        return getStringBounds(gr, string).getWidth();
//    }

    public static Double getStringCenterX(Graphics gr, String string) {
        return getStringBounds(gr, string).getCenterX();
    }

    public static Double getStringCenterY(Graphics gr, String string) {
        return getStringBounds(gr, string).getCenterY();
    }

    public static Rectangle2D getStringBounds(Graphics gr, String string) {
        return gr.getFontMetrics(gr.getFont()).getStringBounds(string, gr);
    }


    public static int addNewFont(String newFontName) {
        fArr.add(newFontName);
        return getFontID(newFontName);
    }

    public static int getFontID(String fontName) {
        for (int i = 0; i < fArr.size(); i++) {
            if (fArr.get(i).equals(fontName)) {
                return i;
            }
        }

        return -1;
    }

    public static int getFontArraySize() {
        return fArr.size();
    }

    public static Set<Entry<Integer, String>> getAllFontsTable() {
        Map<Integer, String> tmpMap = new LinkedHashMap<>();

        for (int fontCount = 0; fontCount < fArr.size(); fontCount++) {
            tmpMap.put(fontCount, fArr.get(fontCount));
        }

        if (!tmpMap.isEmpty()) {
            return tmpMap.entrySet();
        } else {
            return null;
        }
    }

    private static void fontNotExistsMessage(int ID) {
        JOptionPane.showMessageDialog(null,
                "<html>В FoxFontBuilder нет шрифта с ID " + ID + ".<br>"
                        + "Воспользуйтесь методами для получения количества доступных<br>"
                        + "или методами добавления своего шрифта.",
                "Ошибка!", JOptionPane.WARNING_MESSAGE);
    }

    public static Path getFontsDirectory() {
        return fontsDirectory;
    }

    public static void setFontsDirectory(Path _fontsDirectory) {
        if (Files.notExists(Paths.get("./fonts/"))) {
            log("The fonts path is not exist! (" + _fontsDirectory + ").");
        }
        fontsDirectory = _fontsDirectory;
    }


    public static Font[] getAvailableFonts() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    }

    public static Font getAvailableFontByIndex(int index) {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()[index];
    }

    /**
     * @return index of registered font
     */
    public static int register(Font f, GraphicsEnvironment gEnv) {
        if (gEnv.registerFont(f)) {
            fArr.add(f.getFontName());
            return fArr.indexOf(f.getFontName());
        } else {
            throw new RuntimeException("FoxFontBuilder.register: Can`t register the font " + f);
        }

//		InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream("roboto-bold.ttf");
//		Font font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(48f);
//		Font font = Font.createFont(Font.TRUETYPE_FONT, new File("Fonts\\custom_font.ttf")).deriveFont(12f);
    }

    private static void log(String message) {
        Out.Print(FoxFontBuilder.class, LEVEL.DEBUG, message);
    }
}
