package render.foxLFui;

import render.FoxRender;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolBarUI;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthGraphicsUtils;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class MyToolBarUI extends BasicToolBarUI {
    private static MyToolBarUI instance = null;
    private static PropertyChangeListener psl;

    public MyToolBarUI() {
        psl = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {

            }
        };
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        toolBar.addPropertyChangeListener(psl);
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        toolBar.removePropertyChangeListener(psl);
    }

    public void installUI(JComponent c) {
        // Обязательно оставляем установку UI, реализованную в Basic UI классе
        super.installUI(c);

        // Устанавливаем желаемые настройки
        JToolBar toolBar = (JToolBar) c;
        toolBar.setOpaque(false);
        toolBar.setFocusable(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    public static ComponentUI createUI(JComponent c) {
        // Создаём инстанс нашего UI
        if (instance == null) {
            instance = new MyToolBarUI();
        }
        return instance;
    }

    public void paint(Graphics g, JComponent c) {
        Graphics2D g2d = (Graphics2D) g;
        render.FoxRender.setRender(g2d, FoxRender.RENDER.LOW);

        g2d.setPaint(new GradientPaint(0, 0, Color.DARK_GRAY, 0, c.getHeight(),
                new Color(31, 63, 90)));

        g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 3, 3);

        super.paint(g, c);
    }
}
