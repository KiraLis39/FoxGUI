package fox.render.fox_lfui;

import fox.FoxRender;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolBarUI;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.PropertyChangeListener;

public class MyToolBarUI extends BasicToolBarUI {
    private static MyToolBarUI instance = null;
    private final PropertyChangeListener pcl;
    private final FoxRender render = new FoxRender();

    public MyToolBarUI() {
        pcl = event -> {

        };
    }

    public static ComponentUI createUI(JComponent component) {
        // Создаём инстанс нашего UI
        if (instance == null) {
            instance = new MyToolBarUI();
        }
        return instance;
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        toolBar.addPropertyChangeListener(pcl);
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        toolBar.removePropertyChangeListener(pcl);
    }

    @Override
    public void installUI(JComponent c) {
        // Обязательно оставляем установку UI, реализованную в Basic UI классе
        super.installUI(c);

        // Устанавливаем желаемые настройки
        JToolBar toolBar = (JToolBar) c;
        toolBar.setOpaque(false);
        toolBar.setFocusable(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2d = (Graphics2D) g;
        render.setRender(g2d, FoxRender.RENDER.LOW);

        g2d.setPaint(new GradientPaint(0, 0, Color.DARK_GRAY, 0, c.getHeight(),
                new Color(31, 63, 90)));

        g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 3, 3);

        super.paint(g, c);
    }
}
