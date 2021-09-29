package fox;

import java.awt.Window;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;


public class InputAction {
	static Map<String, JComponent> compMap = new LinkedHashMap<String, JComponent>();

	public static void add(String name, Window window) {add(name, (JComponent) window.getComponent(0));}
	
	public static void add(String name, JComponent comp) {compMap.put(name, comp);}
	
	
	public static void set(String name, String commandName, int key, int mod, AbstractAction action) {
		if (compMap.containsKey(name)) {
			((JComponent) compMap.get(name)).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key, mod), commandName);
			((JComponent) compMap.get(name)).getActionMap().put(commandName, action);
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