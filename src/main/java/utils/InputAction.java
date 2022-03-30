package utils;

import lombok.Data;

import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

@Data
public class InputAction {
	public enum FOCUS_TYPE {WHEN_FOCUSED, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, WHEN_IN_FOCUSED_WINDOW}
	private static FOCUS_TYPE focusType = FOCUS_TYPE.WHEN_IN_FOCUSED_WINDOW;
	private static Map<String, JComponent> compMap = new LinkedHashMap<>();

	public static void add(String name, Window window) {add(name, (JComponent) window.getComponent(0));}
	
	public static void add(String name, JComponent comp) {
		compMap.put(name, comp);
	}

	public static void set(FOCUS_TYPE _focusType, String name, String commandName, int key, int mod, AbstractAction action) {
		focusType = _focusType;
		set(name, commandName, key, mod, action);
	}

	public static void set(String name, String commandName, int key, int mod, AbstractAction action) {
		if (compMap.containsKey(name)) {
			(compMap.get(name)).getInputMap(focusType.ordinal()).put(KeyStroke.getKeyStroke(key, mod), commandName);
			(compMap.get(name)).getActionMap().put(commandName, action);
			return;
		}
		
		throw new RuntimeException("InputAction: Error!\nMap of InputAction has not contents component '" + name + "'.");
	}

	public static String list() {return "InputAction: Content:\n" + Arrays.toString(compMap.keySet().toArray());}
	
	public static Set<Entry<String, JComponent>> getEntrySet() {return compMap.entrySet();}
	
	public static void remove(String componentName) throws Exception {
		if (compMap.containsKey(componentName)) {
			compMap.remove(componentName);
			return;
		}
		
		throw new Exception("InputAction: Error!\nMap of InputAction has not contents component '" + componentName + "'.");
	}
	
	public static void clearAll() {compMap.clear();}
}
