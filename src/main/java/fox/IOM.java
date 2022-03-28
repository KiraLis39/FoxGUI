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

@Deprecated
public class IOM {
	public enum HEADERS {REGISTRY, CONFIG, USER_SAVE, LAST_USER, USER_LIST, SECURE, TEMP} // несколько готовых примеров (можно задавать свои)
	
	private final static ArrayList<Properties> propsArray = new ArrayList<>(HEADERS.values().length); // массив активных хранилищ
	private static Boolean consoleOut = false; // трассировка в лог
	private static String DEFAULT_EMPTY_STRING = "NA"; // чем будет значение при его отсутствии\создании без указания значения

	private IOM() {}
	
	
	// подключение хранилища:
	public synchronized static void add(Object propertiName, File PropertiFile) {
		String name = propertiName.toString();

		// проверяем не идет ли повторная попытка создания такого же проперчеса...
		for (Properties p : propsArray) {
			if (p.containsKey("propName") && p.getProperty("propName").equals(name)) {
				log("Такой экземпляр уже есть! Перезапись...");
				propsArray.remove(propsArray.indexOf(p));
			}
		}

		log("Создание проперчеса " + name + " от файла " + PropertiFile);
		Properties tmp = new Properties(4);

		// проверяем есть ли файл для чтения\записи...
		if (isFileExist(PropertiFile)) {
			try (InputStreamReader ISR = new InputStreamReader(new FileInputStream(PropertiFile), StandardCharsets.UTF_8)) {
				tmp.load(ISR);
	
				tmp.setProperty("propName", name);
				tmp.setProperty("propFile", PropertiFile.getPath());

				propsArray.add(tmp);
				save(tmp);
				log("Cоздание нового потока: " + name + " завершено.");
			} catch (Exception ex) {
				log("Проблема при чтении '" + PropertiFile + "': " + ex.getMessage());
				ex.printStackTrace();
			}
		}
	}
	
	// установка\изменение значений в хранилище:
	public synchronized static void set(Object propertiName, Object key, Object value) {
		String name = propertiName.toString(), parameter = key.toString();
		
		if (name.isBlank()) {showWithoutNameErr(name);
		} else if (parameter.isBlank()) {showWithoutKeyErr(parameter);
		} else {
			for (Properties p : propsArray) {
				if (p.containsKey("propName")) {
					if (p.getProperty("propName").equals(name)) {
						log("Запись в " + name + " параметра " + value + "' (" + value.getClass().getTypeName() + ").");
						if (p.containsKey(parameter)) {p.setProperty(parameter, String.valueOf(value));
						} else {p.putIfAbsent(parameter, String.valueOf(value));}
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
			for (int i = 0; i < propsArray.size(); i++) {
				Properties p = propsArray.get(i);
				if (!p.containsKey("propName")) {
					log("Каким-то образом проперчес " + p + " не имеет ключа с именем.");
					continue;
				}
				
				if (p.getProperty("propName").equals(name)) {
					// если хранилище имеет такое значение, и это не заглушка - просто выходим:
					if (p.containsKey(parameter) && !p.get(parameter).equals(DEFAULT_EMPTY_STRING)) {return;
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
			propsArray.get(ind).putIfAbsent(key.toString(), "false");
			
			log("Возврат флага " + propsArray.get(ind).getProperty(key.toString()) + ".");
			return Boolean.valueOf(propsArray.get(ind).getProperty(key.toString()));
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
				propsArray.get(ind).putIfAbsent(key.toString(), "-1D");
				save(propertiName.toString());
			}
			return Double.parseDouble(propsArray.get(ind).getProperty(key.toString()));
		} catch (Exception e) {
			e.printStackTrace();
			log("Параметр '" + key + "' не является типом Double. (" + key.getClass() + ")" + (ind == -1 ? "" : " (value: " + propsArray.get(ind).getProperty(key.toString()) + ")."));
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
			propsArray.get(ind).putIfAbsent(key.toString(), DEFAULT_EMPTY_STRING);
			save(propertiName.toString());
		}
		return propsArray.get(ind).getProperty(key.toString());
	}
	// пытаемся взять инт из хранилища:
	public synchronized static Integer getInt(Object propertiName, Object key) {
		if (!existProp(propertiName.toString())) {
			showNotExistsErr(propertiName.toString());
			return -1;
		}
		
		try {
			int ind = getPropIndex(propertiName.toString());
			Properties tmp = propsArray.get(ind);
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
			for (int i = 0; i < propsArray.size(); i++) {
				if (propsArray.get(i).containsKey("propName")) {
					if (propsArray.get(i).getProperty("propName").equals(propertiName.toString())) {
						log("Удаление из проперчес " + propertiName + " параметра " + key + "'.");
						propCount = i;
						break;
					}
				}
			}

			if (propCount == -1) {showNotExistsErr(propertiName.toString());
			} else {
				if (propsArray.get(propCount).containsKey(key)) {propsArray.get(propCount).remove(key);}
			}
		}
	}


	// сохранить конкретное хранилище на диск:
	public synchronized static Boolean save(Properties property) {
		return save(property.getProperty("propName"));
	}

	public synchronized static Boolean save(String propertiName) {
		for (Properties properties : propsArray) {
			if (properties.getProperty("propName").equals(propertiName)) {
				try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(properties.getProperty("propFile"), false), StandardCharsets.UTF_8)) {
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
		for (Properties properties : propsArray) {save(properties);}
	}


	// загрузить конкретное хранилище из файла на диске:
	public synchronized static Boolean load(Properties property) {
		return load(property.getProperty("propName"));
	}

	public synchronized static Boolean load(String propertiName) {
		for (Properties properties : propsArray) {
			if (properties.get("propName").equals(propertiName.toString())) {
				try (InputStreamReader ISR = new InputStreamReader(new FileInputStream(properties.getProperty("propFile")), StandardCharsets.UTF_8)) {
					log("Загрузка файла " + propertiName + " в поток " + properties.getProperty("propName"));
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
		for (Properties properties : propsArray) {load(properties);}
	}

	
	private static void showNotExistsErr(Object data) {log("Не найден поток '" + data + "'.");}
	private static void showWithoutNameErr(Object data) {log("Запись в проперчес имени невозможна: " + data);}
	private static void showWithoutKeyErr(Object data) {log("Запись в проперчес ключа невозможна: " + data);}
	private static void showLoadStreamErr(Object data) {log("Проблема с выгрузкой потока " + data + " в файл!");}
	
	// существует ли активное хранилище:
	public synchronized static Boolean existProp(String propertiName) {
		for (Properties properti : propsArray) {
			if (properti.get("propName").equals(propertiName)) {return true;}
		}
		return false;
	}
	// существует ли в хранилище такой ключ:
	public synchronized static Boolean existKey(String propertiName, String key) {
		for (Properties properties : propsArray) {
			if (properties.get("propName").equals(propertiName)) {
				if (properties.containsKey(key)) {return true;}
			}
		}

		return false;
	}
	// получить индекс хранилища с таким именем:
	public synchronized static int getPropIndex(String propertiName) {
		for (Properties properti : propsArray) {
			if (properti.get("propName").equals(propertiName)) {return propsArray.indexOf(properti);}
		}
		return -1;
	}
	// получить список имен активных хранилищ:
	public synchronized static String getPropsNames() {
		ArrayList<String> propsNames = new ArrayList<String>(propsArray.size());
		for (Properties properties : propsArray) {propsNames.add(properties.getProperty("propName"));}
		return Arrays.toString(propsNames.toArray());
	}
	
	// проверка директорий и файлов хранилищ:
	private static boolean isFileExist(File file) {
		Path parentDir = file.getParentFile().toPath();
		while (Files.notExists(parentDir)) {
			log("Попытка создания директории '" + parentDir + "'...");
			
			try {Files.createDirectory(parentDir);
			} catch (IOException i0) {
				log("Провал создания директории '" + file + "': " + i0.getMessage());
				i0.printStackTrace();
				return false;
			}
		}
		
		Path self = file.toPath();
		if (Files.notExists(self)) {
			log("Попытка создания файла '" + file + "'...");
			
			try {Files.createFile(self);
			} catch (IOException e1) {
				log("Провал создания файла '" + file + "': " + e1.getMessage());
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
