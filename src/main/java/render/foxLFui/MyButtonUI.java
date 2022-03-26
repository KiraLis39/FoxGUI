package render.foxLFui;

import render.FoxRender;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

public class MyButtonUI extends BasicButtonUI {
    private static MyButtonUI instance = null;

    public void installUI(JComponent c) {
        // Обязательно оставляем установку UI, реализованную в Basic UI классе
        super.installUI(c);

        // Устанавливаем желаемые настройки JButton'а
        // Для абстракции используем AbstractButton, так как в нём есть всё необходимое нам
        AbstractButton button = (AbstractButton) c;
        button.setOpaque(false);
        button.setFocusable(true);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    public static ComponentUI createUI(JComponent c) {
        // Создаём инстанс нашего UI
        if (instance == null) {
            instance = new MyButtonUI();
        }
        return instance;
    }

    public void paint(Graphics g, JComponent c) {
        Graphics2D g2d = (Graphics2D) g;
        render.FoxRender.setRender(g2d, FoxRender.RENDER.LOW);

        AbstractButton button = (AbstractButton) c;
        ButtonModel buttonModel = button.getModel();

        // Формой кнопки будет закруглённый прямоугольник

        // Фон кнопки
        if (buttonModel.isPressed()) {
            g2d.setPaint(new GradientPaint(0, 0, Color.DARK_GRAY, 0, c.getHeight(),
                    new Color(31, 63, 90)));
        } else {
            g2d.setPaint(new GradientPaint(0, 0, Color.BLACK, 0, c.getHeight(),
                    new Color(31, 63, 90)));
        }

        // Закгругление необходимо делать больше, чем при отрисовке формы,
        // иначе светлый фон будет просвечивать по краям
        g2d.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 9, 9);

        if (button.isBorderPainted()) {
            // Бордер кнопки
            g2d.setPaint(Color.DARK_GRAY);
            // Важно помнить, что форму необходимо делать на 1px меньше, чем ширина/высота компонента,
            // иначе правый и нижний края фигуры вылезут за границу компонента и не будут видны
            // К заливке это не относится, так как последняя колонка/строка пикселей игнорируется при заполнении
            g2d.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 9, 9);
        }

        // Сдвиг отрисовки при нажатой кнопке
        if (buttonModel.isPressed()) {
            g2d.translate(0.25, 0.25);
        }

        // Отрисовка текста и иконки изображения
        button.setForeground(Color.WHITE);
        super.paint(g, button);
    }
}
