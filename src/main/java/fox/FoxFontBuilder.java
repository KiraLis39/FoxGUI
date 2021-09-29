package fox;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.JOptionPane;


public class FoxFontBuilder {
	public enum FONT {COMIC_SANS, MONOTYPE_CORSIVA, BAHNSCHRIFT, CANDARA, HARLOW_S_I, CORBEL, GEORGIA, ARIAL, ARIAL_NARROW, 
		SEGOE_SCRIPT, CAMBRIA, CONSTANTIA, CONSOLAS, PAPYRYS, LEELAWADEE, SEGOE_UI_SYMBOL, TIMES_NEW_ROMAN}
	private static FONT defaultFont = FONT.ARIAL_NARROW;
	
	private static List<String> fArr = new LinkedList<String>(); // набор шрифтов по-умолчанию.
	private static File fontsDirectory; // папка с дополнительными шрифтами TRUETYPE
		
	static {
		fArr.add(0, "Comic Sans MS");			fArr.add(1, "Monotype Corsiva");
		fArr.add(2, "bahnschrift");				fArr.add(3, "Candara");
		fArr.add(4, "Harlow Solid Italic");		fArr.add(5, "Corbel");
		fArr.add(6, "Georgia");					fArr.add(7, "Arial");
		fArr.add(8, "Arial Narrow");			fArr.add(9, "Segoe Script");
		fArr.add(10, "Cambria");				fArr.add(11, "Constantia");
		fArr.add(12, "Consolas");				fArr.add(13, "Papyrus");
		fArr.add(14, "Leelawadee UI");			fArr.add(15, "Segoe UI Symbol");
		fArr.add(16, "Times New Roman");
	}

	public FoxFontBuilder(File customFontsDir) {
		if (customFontsDir != null && Files.exists(customFontsDir.toPath())) {fontsDirectory = customFontsDir;}
	}

	// выбор шрифта:
	public static Font setFoxFont(FONT fontName, float fontSize) {return setFoxFont(fontName.ordinal(), Math.round(fontSize), false);}	
	public static Font setFoxFont(FONT fontName, float fontSize, Boolean isBold) {return setFoxFont(fontName.ordinal(), Math.round(fontSize), isBold);}	
	public static Font setFoxFont(int ID, int fontSize, Boolean isBold) {
		if (fArr.size() == 0) {new FoxFontBuilder(null);}
		
		if (ID > fArr.size() - 1) {
			errMessage(ID);
			return new Font(fArr.get(defaultFont.ordinal()), isBold ? Font.BOLD : Font.PLAIN, fontSize);
		}
		
	    if (!isFontExist(ID)) {
	    	if (fontsDirectory == null) {return new Font(fArr.get(defaultFont.ordinal()), isBold ? Font.BOLD : Font.PLAIN, fontSize);} // если шрифт не зарегистрирован и не указана дирректория с требуемым - возвращаем шрифт по-умолчанию.
	    	
	    	// если в ОС нет шрифта, но указана папка с необходимыми шрифтами:
	    	try {
	    		log("Now will be setup fonts...");
	    		for (File fontDir : fontsDirectory.listFiles()) {
					File[] fonts = fontDir.listFiles();
					for (File font : fonts) {
						try {
							registerFont(Font.createFont(Font.TRUETYPE_FONT, font));
						} catch (Exception e) {
							log("Не удалось подключить шрифт " + font.getName() + " как TRUETYPE." + e.getMessage());
							continue;
						}
					}
				}
	    	} catch (Exception e) {
				e.printStackTrace();
				log("Error with font existing! Set fonts dir by methode:  setFontsDirectory(File fontsDirectory) where fontsDirectory is a folder with fonts from FoxLib jar archive.fonts");
	    		if (!fontsDirectory.exists()) {log("FAILED!");	    			
	    		} else {log("Success!");}
	    	}
	    	
	    	// если не получилось, возвращаем шрифт по-умолчанию:
	    	if (!isFontExist(ID)) {return new Font(fArr.get(defaultFont.ordinal()), isBold ? Font.BOLD : Font.PLAIN, fontSize);}	    	
	    }
	    
		return new Font(fArr.get(ID), isBold ? Font.BOLD : Font.PLAIN, fontSize);
	}

	private static boolean isFontExist(int ID) {
	    for (String fname : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
	        if (fname.equalsIgnoreCase(fArr.get(ID))) {return true;}
	    }
	    
	    log("Font '" + fArr.get(ID) + "' not exists in this OS! Please setup it if you can.");
		return false;
	}

	public static int getStringHeight(Graphics gr) {return gr.getFontMetrics().getHeight();}
	public static Double getStringWidth(Graphics gr, String string) {return getStringBounds(gr, string).getWidth();}
	
	public static Double getStringCenterX(Graphics gr, String string) {return getStringBounds(gr, string).getCenterX();}
	public static Double getStringCenterY(Graphics gr, String string) {return getStringBounds(gr, string).getCenterY();}
	
	public static Rectangle2D getStringBounds(Graphics gr, String string) {return gr.getFontMetrics(gr.getFont()).getStringBounds(string, gr);}
		
	
	public static int addNewFont(String newFontName) {
		fArr.add(newFontName);
		return getFontID(newFontName);
	}

	public static int getFontID(String fontName) {
		for (int i = 0; i < fArr.size(); i++) {
			if (fArr.get(i).equals(fontName)) {return i;}
		}
		
		return -1;
	}

	public static int getFontArraySize() {return fArr.size();}

	public static Set<Entry<Integer, String>> getAllFontsTable() {
		Map<Integer, String> tmpMap = new LinkedHashMap<>();
		
		for (int fontCount = 0; fontCount < fArr.size(); fontCount++) {
			tmpMap.put(fontCount, fArr.get(fontCount));
		}
		
		if (!tmpMap.isEmpty()) {return tmpMap.entrySet();} else {return null;}
	}
	
	private static void errMessage(int ID) {
		JOptionPane.showMessageDialog(null,
				"<html>В FoxFontBuilder нет шрифта с ID " + ID + ".<br>"
				+ "Воспользуйтесь методами для получения количества доступных<br>"
				+ "или методами добавления своего шрифта.",
				"Ошибка!", JOptionPane.WARNING_MESSAGE);
	}
	
	public static File getFontsDirectory() {return fontsDirectory;}
	public static void setFontsDirectory(File _fontsDirectory) {fontsDirectory = _fontsDirectory;}

	
	public static Font[] getSystemFonts() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();		
	}
	
	public static Font getSystemFont(int index) {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()[index];		
	}
	
	public static boolean registerFont(Font f) {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
		
//		InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream("roboto-bold.ttf");
//		Font font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(48f);
//		Font font = Font.createFont(Font.TRUETYPE_FONT, new File("Fonts\\custom_font.ttf")).deriveFont(12f);
	}

	
	private static void log(String message) {
		System.out.println(FoxFontBuilder.class.getName() + ": " + message);
	}
}
