package fox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;


public class IOM {
	public static enum HEADERS {REGISTRY, CONFIG, USER_SAVE, LAST_USER, USER_LIST, SECURE, TEMP} // несколько готовых примеров (можно задавать и свои, конечно)
	
	private final static ArrayList<Properties> PropsArray = new ArrayList<Properties>(HEADERS.values().length); // массив активных хранилищ
	private final static Charset codec = StandardCharsets.UTF_8;
	
	private static Boolean consoleOut = false; // трассировка в лог
	private static String DEFAULT_EMPTY_STRING = "NA"; // чем будет значение при его отсутствии\создании без указания значения
	
	
	private IOM() {}
	
	
	// подключение нового хранилища:
	public synchronized static void add(Object propertiName, File PropertiFile) {
		String name = propertiName.toString();

		// проверяем не идет ли повторная попытка создания такого же проперчеса...
		for (int i = 0; i < PropsArray.size(); i++) {
			if (PropsArray.get(i).containsKey("propName") && PropsArray.get(i).getProperty("propName").equals(name)) {
				log("Такой экземпляр уже есть! Перезапись...");
				PropsArray.remove(i);
			}
		}

		log("Создание проперчеса " + name + " от файла " + PropertiFile);
		Properties tmp = new Properties(4);

		// проверяем есть ли файл для чтения\записи...
		if (testFileExist(PropertiFile)) {
			try (InputStreamReader ISR = new InputStreamReader(new FileInputStream(PropertiFile), codec)) {
				tmp.load(ISR);
	
				tmp.setProperty("propName", name);
				tmp.setProperty("propFile", PropertiFile.getPath());
	
				PropsArray.add(tmp);
	
				save(name);
				log("Cоздание нового потока: " + name + " завершено.");
			} catch (Exception ex) {log("Проблема при чтении файла последнего пользователя lastUserFile!");}
		}
	}
	
	// установка\изменение значений в хранилище:
	public synchronized static void set(Object propertiName, Object key, Object value) {
		String name = propertiName.toString(), parameter = key.toString();
		
		if (name.isEmpty()) {showWithoutNameErr(name);
		} else if (parameter.equals("")) {showWithoutKeyErr(parameter);
		} else {
			for (int i = 0; i < PropsArray.size(); i++) {
				if (PropsArray.get(i).containsKey("propName")) {
					if (PropsArray.get(i).getProperty("propName").equals(name)) {
						log("Запись в проперчес " + name + " параметра " + String.valueOf(value) + "' (" + value.getClass().getTypeName() + ").");
						if (PropsArray.get(i).containsKey(parameter)) {PropsArray.get(i).setProperty(parameter, String.valueOf(value));
						} else {PropsArray.get(i).putIfAbsent(parameter, String.valueOf(value));}
						return;
					}
				}
			}

			showNotExistsErr(name);
		}
	}

	// первичная установка\проверка установленности значения:
	public synchronized static void setIfNotExist(Object propertiName, Object existKey, Object defaultValue) {
		String name = propertiName.toString(), parameter = existKey.toString();
		
		if (name.isEmpty() || name.isBlank()) {showWithoutNameErr(name);
		} else if (parameter.isEmpty() || parameter.isBlank()) {showWithoutKeyErr(parameter);
		} else {
			for (int i = 0; i < PropsArray.size(); i++) {
				if (!PropsArray.get(i).containsKey("propName")) {
					log("Каким-то образом проперчес " + PropsArray.get(i).toString() + " не имеет ключа с именем.");
					continue;
				}
				
				if (PropsArray.get(i).getProperty("propName").equals(name)) {
					// если хранилище имеет такое значение, и это не заглушка - просто выходим:
					if (PropsArray.get(i).containsKey(parameter) && !PropsArray.get(i).get(parameter).equals(DEFAULT_EMPTY_STRING)) {return;
					} else {set(propertiName, existKey, defaultValue);} // иначе устанавливаем новое значение.
					return;
				}
			}

			showNotExistsErr(name);
		}
	}
	
	// пытаемся взять булен из хранилища:
	public synchronized static Boolean getBoolean(Object propertiName, Object key) {
		if (!existProp(propertiName.toString())) {
			showNotExistsErr(propertiName.toString());
			return false;
		}
		
		try {
			int ind = getPropIndex(propertiName.toString());
			log("Конфигурация найдена. Чтение флага " + key.toString() + "...");				
			PropsArray.get(ind).putIfAbsent(key.toString(), "false");
			
			log("Возврат флага " + PropsArray.get(ind).getProperty(key.toString()) + ".");				
			return Boolean.valueOf(PropsArray.get(ind).getProperty(key.toString()));
		} catch (Exception e) {
			e.printStackTrace();
			log("Параметр '" + key + "' не является типом Boolean. (" + key.getClass() + ")");
			return false;
		}
	}
	// пытаемся взять дабл из хранилища:
	public synchronized static Double getDouble(Object propertiName, Object key) {
		if (!existProp(propertiName.toString())) {
			showNotExistsErr(propertiName.toString());
			return -1D;
		}
		
		int ind = -1;		
		try {
			ind = getPropIndex(propertiName.toString());
			if (!existKey(propertiName.toString(), key.toString())) {
				PropsArray.get(ind).putIfAbsent(key.toString(), "-1D");
				save(propertiName.toString());
			}
			return Double.parseDouble(PropsArray.get(ind).getProperty(key.toString()));
		} catch (Exception e) {
			e.printStackTrace();
			log("Параметр '" + key + "' не является типом Double. (" + key.getClass() + ")" + (ind == -1 ? "" : " (value: " + PropsArray.get(ind).getProperty(key.toString()) + ")."));
		}
		
		return null;
	}
	// берём стринг:
	public synchronized static String getString(Object propertiName, Object key) {
		if (!existProp(propertiName.toString())) {
			showNotExistsErr(propertiName.toString());
			return null;
		}
		
		int ind = getPropIndex(propertiName.toString());
		if (!existKey(propertiName.toString(), key.toString())) {
			PropsArray.get(ind).putIfAbsent(key.toString(), DEFAULT_EMPTY_STRING);
			save(propertiName.toString());
		}
		return PropsArray.get(ind).getProperty(key.toString());
	}
	// пытаемся взять инт из хранилища:
	public synchronized static Integer getInt(Object propertiName, Object key) {
		if (!existProp(propertiName.toString())) {
			showNotExistsErr(propertiName.toString());
			return -1;
		}
		
		try {
			int ind = getPropIndex(propertiName.toString());
			Properties tmp = PropsArray.get(ind);
			if (!existKey(propertiName.toString(), key.toString())) {
				tmp.putIfAbsent(key.toString(), "-1");
				save(propertiName.toString());
			}
			
			return Integer.parseInt(tmp.getProperty(key.toString()));
		} catch (Exception e) {
			e.printStackTrace();
			log("Параметр '" + key + "' не является типом Integer. (" + key.getClass() + ")");
		}
		
		return null;
	}
    // удаление строки данных из хранилища:
	public synchronized static void remove(Object propertiName, Object key) {
		if (propertiName.toString().isEmpty() || propertiName.toString().isBlank()) {showWithoutNameErr(propertiName.toString());
		} else {
			int propCount = -1;
			for (int i = 0; i < PropsArray.size(); i++) {
				if (PropsArray.get(i).containsKey("propName")) {
					if (PropsArray.get(i).getProperty("propName").equals(propertiName.toString())) {
						log("Удаление из проперчес " + propertiName.toString() + " параметра " + key + "'.");
						propCount = i;
						break;
					}
				}
			}

			if (propCount == -1) {showNotExistsErr(propertiName.toString());
			} else {
				if (PropsArray.get(propCount).containsKey(key)) {PropsArray.get(propCount).remove(key);}
			}
		}
	}
	
	// сохранить конкретное хранилище на диск:
	public synchronized static Boolean save(Object propertiName) {
		for (Properties properties : PropsArray) {
			if (properties.get("propName").equals(propertiName.toString())) {
				try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File(properties.getProperty("propFile")), false), codec)) {
					properties.store(osw, "IOM_SAVE");
					log("Сохранение " + properties.getProperty("propFile") + " завершено!");
					return true;
				} catch (IOException e) {
					showLoadStreamErr(properties);
					e.printStackTrace();
					return false;
				}
			}
		}
		
		log("Не найден поток " + propertiName.toString() + ".");
		return false;
	}
	// сохранить все активные хранилища на диск:
	public synchronized static void saveAll() {
		log("Каскадное сохранение всех файлов...");
		for (Properties properties : PropsArray) {save(properties.get("propName"));}
	}

	// загрузить конкретное хранилище из файла на диске:
	public synchronized static Boolean load(Object propertiName) {
		for (Properties properties : PropsArray) {
			if (properties.get("propName").equals(propertiName.toString())) {
				try (InputStreamReader ISR = new InputStreamReader(new FileInputStream(new File(properties.getProperty("propFile"))), codec)) {
					log("Загрузка файла " + propertiName.toString() + " в поток " + properties.getProperty("propName"));
					properties.load(ISR);
					return true;
				} catch (IOException e) {
					log("Проблема с загрузкой потока " + properties.getProperty("propName") + "!");
					e.printStackTrace();
					break;
				}				
			}
		}

		return false;
	}
	// загрузить все хранилища из файлов на диске:
	public synchronized static void loadAll() {
		log("Каскадная загрузка всех файлов (перезагрузка проперчесов)...");
		for (Properties properties : PropsArray) {load(properties);}
	}

	
	private static void showNotExistsErr(Object data) {log("Не найден поток '" + data + "'.");}
	private static void showWithoutNameErr(Object data) {log("Запись в проперчес имени невозможна: " + data);}
	private static void showWithoutKeyErr(Object data) {log("Запись в проперчес ключа невозможна: " + data);}
	private static void showLoadStreamErr(Object data) {log("Проблема с выгрузкой потока " + data + " в файл!");}
	
	// существует ли активное хранилище:
	public synchronized static Boolean existProp(String propertiName) {
		for (Properties properti : PropsArray) {
			if (properti.get("propName").equals(propertiName.toString())) {return true;}
		}
		return false;
	}
	// существует ли в хранилище такой ключ:
	public synchronized static Boolean existKey(String propertiName, String key) {
		for (Properties properties : PropsArray) {
			if (properties.get("propName").equals(propertiName.toString())) {
				if (properties.containsKey(key)) {return true;}
			}
		}

		return false;
	}
	// получить индекс хранилища с таким именем:
	public synchronized static int getPropIndex(String propertiName) {
		for (Properties properti : PropsArray) {
			if (properti.get("propName").equals(propertiName)) {return PropsArray.indexOf(properti);}
		}
		return -1;
	}
	// получить список имен активных хранилищ:
	public synchronized static String getPropsNames() {
		ArrayList<String> propsNames = new ArrayList<String>(PropsArray.size());
		for (Properties properties : PropsArray) {propsNames.add(properties.getProperty("propName"));}
		return Arrays.toString(propsNames.toArray());
	}
	
	// проверка директорий и файлов хранилищ:
	private static boolean testFileExist(File file) {
		Path parentDir = Paths.get(file.getParentFile().toURI());
		while (Files.notExists(parentDir)) {
			log("Попытка создания директории '" + parentDir + "'...");
			
			try {Files.createDirectory(parentDir);
			} catch (IOException i0) {
				i0.printStackTrace();
				return false;
			}
		}
		
		Path self = Paths.get(file.toURI());
		while (Files.notExists(self)) {
			log("Попытка создания файла '" + file + "'...");
			
			try {Files.createFile(self);
			} catch (IOException e1) {
				e1.printStackTrace();
				return false;
			}
		}
		
		return true;
	}
	
	public static void setDefaultEmptyString(String des) {DEFAULT_EMPTY_STRING = des;}
	
	// выводить ли события в лог:
	public static Boolean isConsoleOutOn() {return consoleOut;}
	public static void setConsoleOutOn(Boolean onOff) {consoleOut = onOff;}
	
	// вывод событий в лог:
	private static void log(String message) {
		if (consoleOut) {System.out.println(IOM.class.getName() + ": " + message);}
	}
}
