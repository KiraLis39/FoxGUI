package fox.render.fox_lfui;

import fox.FoxFontBuilder;
import fox.FoxRender;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

public class MyButtonUI extends BasicButtonUI {
    private static MyButtonUI instance = null;
    private final FoxRender render = new FoxRender();
    private final FoxFontBuilder ffb = new FoxFontBuilder();

    public static ComponentUI createUI(JComponent component) {
        // Создаём инстанс нашего UI
        if (instance == null) {
            instance = new MyButtonUI();
        }
        return instance;
    }

    @Override
    public void installUI(JComponent c) {
        // Обязательно оставляем установку UI, реализованную в Basic UI классе
        super.installUI(c);

        // Устанавливаем желаемые настройки JButton'а
        // Для абстракции используем AbstractButton, так как в нём есть всё необходимое нам
        AbstractButton button = (AbstractButton) c;
        button.setOpaque(false);
        button.setFocusable(true);
        button.setIgnoreRepaint(true);
        button.setFocusPainted(false);
        button.setDoubleBuffered(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2d = (Graphics2D) g;
        render.setRender(g2d, FoxRender.RENDER.MED, true, false);

        AbstractButton button = (AbstractButton) c;

        // Формой кнопки будет закруглённый прямоугольник

        // Фон кнопки
        if (button.getModel().isPressed()) {
            g2d.setPaint(new GradientPaint(0, 0, Color.BLACK, 0, c.getHeight(),
                    new Color(
                            Math.max(15, button.getBackground().getRed() - 63),
                            Math.max(15, button.getBackground().getGreen() - 63),
                            Math.max(15, button.getBackground().getBlue() - 63))));
        } else if (button.getModel().isRollover()) {
            g2d.setPaint(new GradientPaint(0, 0, new Color(
                    Math.min(255, button.getBackground().getRed() + 15),
                    Math.min(255, button.getBackground().getGreen() + 15),
                    Math.min(255, button.getBackground().getBlue() + 15)), 0, c.getHeight(), Color.BLACK));
        } else {
            g2d.setPaint(new GradientPaint(0, 0, new Color(
                    button.getBackground().getRed(),
                    button.getBackground().getGreen(),
                    button.getBackground().getBlue()), 0, c.getHeight(), Color.BLACK));
        }

        // Закругление необходимо делать больше, чем при отрисовке формы,
        // иначе светлый фон будет просвечивать по краям
        g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 9, 9);

        if (button.isBorderPainted()) {

            if (button.getModel().isRollover()) {
                final int iterationsCount = Math.min(48, Math.max(32, button.getWidth())) / 15;
                int alpha = 127;
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.setPaint(new Color(
                        button.getBackground().getRed(),
                        button.getBackground().getGreen(),
                        button.getBackground().getBlue(), alpha));
                g2d.drawRoundRect(1, 1, c.getWidth() - 2, c.getHeight() - 2, 9, 9);
                for (int i = 1; i <= iterationsCount; i++) {
                    alpha -= 95 / iterationsCount;
                    g2d.setPaint(new Color(
                            button.getBackground().getRed() / 2,
                            button.getBackground().getGreen() / 2,
                            button.getBackground().getBlue() / 2, alpha));
                    g2d.drawRoundRect(i, i, c.getWidth() - (i * 2), c.getHeight() - (i * 2),
                            (int) (8f + (i * 2)), (int) (8f + (i * 2)));
                }
            } else {
                // Бордер кнопки
                g2d.setPaint(Color.BLACK);
                // Важно помнить, что форму необходимо делать на 1px меньше, чем ширина/высота компонента,
                // иначе правый и нижний края фигуры вылезут за границу компонента и не будут видны
                // К заливке это не относится, так как последняя колонка/строка пикселей игнорируется при заполнении
                g2d.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 9, 9);
            }
        }

        // Сдвиг отрисовки текста и иконки при нажатой кнопке
        if (button.getModel().isPressed()) {
            g2d.translate(0.475, 0.475);
        }
        // Отрисовка текста и иконки изображения
        g2d.setColor(button.getForeground());
        super.paint(g2d, button);

        g2d.dispose();
    }
}
