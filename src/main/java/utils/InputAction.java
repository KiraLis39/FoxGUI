package utils;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Data
@Component
public final class InputAction {
    private final Map<String, JComponent> compMap = new LinkedHashMap<>();
    private FOCUS_TYPE focusType = FOCUS_TYPE.WHEN_IN_FOCUSED_WINDOW;

    public void add(String name, Window window) {
        add(name, (JComponent) window.getComponent(0));
    }

    public void add(String name, JComponent comp) {
        compMap.put(name, comp);
    }

    public void set(FOCUS_TYPE _focusType, String name, String commandName, int key, int mod, AbstractAction action) {
        focusType = _focusType;
        set(name, commandName, key, mod, action);
    }

    public void set(String name, String commandName, int key, int mod, AbstractAction action) {
        if (compMap.containsKey(name)) {
            (compMap.get(name)).getInputMap(focusType.ordinal()).put(KeyStroke.getKeyStroke(key, mod), commandName);
            (compMap.get(name)).getActionMap().put(commandName, action);
            return;
        }

        throw new RuntimeException("InputAction: Error!\nMap of InputAction has not contents component '" + name + "'.");
    }

    public String list() {
        return "InputAction: Content:\n" + Arrays.toString(compMap.keySet().toArray());
    }

    public Set<Entry<String, JComponent>> getEntrySet() {
        return compMap.entrySet();
    }

    public void remove(String componentName) throws Exception {
        if (compMap.containsKey(componentName)) {
            compMap.remove(componentName);
            return;
        }

        throw new Exception("InputAction: Error!\nMap of InputAction has not contents component '" + componentName + "'.");
    }

    public void clearAll() {
        compMap.clear();
    }

    public enum FOCUS_TYPE {WHEN_FOCUSED, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, WHEN_IN_FOCUSED_WINDOW}
}
