package fox.utils;

import lombok.extern.slf4j.Slf4j;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Slf4j
public class InputAction {
    private final Map<String, JComponent> compMap = new LinkedHashMap<>();

    public void add(String name, JComponent comp) {
        comp.setFocusable(true);
        comp.setRequestFocusEnabled(true);
        compMap.put(name, comp);
    }

    public void set(int jComponentFocusType, String name, String commandName, int key, int mod, AbstractAction action) {
        set(jComponentFocusType, name, commandName, key, mod, false, action);
    }

    public void set(int jComponentFocusType, String name, String commandName, KeyStroke keyStroke, AbstractAction action) {
        set(jComponentFocusType, name, commandName, keyStroke.getKeyCode(), keyStroke.getModifiers(), keyStroke.isOnKeyRelease(), action);
    }

    public void set(int jComponentFocusType, String name, String commandName, int key, int mod, boolean onRelease, AbstractAction action) {
        if (compMap.containsKey(name)) {
            (compMap.get(name)).getInputMap(jComponentFocusType).put(KeyStroke.getKeyStroke(key, mod, onRelease), commandName);
            (compMap.get(name)).getActionMap().put(commandName, action);
            return;
        }

        log.error("InputAction: Map of InputAction has not contents component '{}'.", name);
    }

    public String list() {
        return "InputAction content: " + compMap.keySet();
    }

    public Set<Entry<String, JComponent>> getEntrySet() {
        return compMap.entrySet();
    }

    public void remove(String componentName) {
        if (compMap.containsKey(componentName)) {
            compMap.remove(componentName);
            return;
        }

        log.error("InputAction: Map of InputAction has not contents component '{}'.", componentName);
    }

    public void clearAll() {
        compMap.clear();
    }
}
