package fox.components;

import fox.FoxRender;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

@Slf4j
public final class FoxTip extends JDialog implements WindowFocusListener, ComponentListener {
    private static final float MAX_OPAQUE = 0.9f;
    @Setter
    private static Color baseColor = new Color(1.0f, 1.0f, 1.0f, 0.3f);
    @Setter
    private static Color secondColor = new Color(0.25f, 0.25f, 0.35f, 0.9f);
    @Setter
    private static Color borderColor = new Color(0.5f, 0.5f, 0.5f, 1.0f);
    private static float opacity = 0.1f;
    private final transient FoxRender render = new FoxRender();
    private final Object aim;
    private final TYPE type;
    private final JPanel contentPanel;
    private final JTextField inputField;
    private Timer timer;

    public FoxTip(TYPE type, BufferedImage icon, String title, String message, String footer, Object aim) {
        this(type, icon, title, message, footer, aim, baseColor, secondColor, borderColor);
    }

    public FoxTip(
            TYPE type, BufferedImage icon, String title, String message, String footer, Object aim,
            Color _baseColor, Color _secondColor, Color _borderColor
    ) {
        this.type = type;
        this.aim = aim;
        baseColor = _baseColor;
        secondColor = _secondColor;
        borderColor = _borderColor;
        this.inputField = type == TYPE.INPUT ? new JTextField() : null;

        setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setBackground(new Color(0, 0, 1, 0.0f));
        setAlwaysOnTop(true);
        setModal(false);

        addWindowFocusListener(this);
        addComponentListener(this);

        contentPanel = new JPanel(new BorderLayout(4, 4)) {
            {
                setOpaque(false);
                setBorder(new EmptyBorder(6, 10, 15, 10));
                setAlignmentY(Component.TOP_ALIGNMENT);

                // left tip icon:
                add(new JLabel() {
                    @Override
                    public void paintComponent(Graphics g) {
                        Graphics2D g2d = (Graphics2D) g;
                        render.setRender(g2d, FoxRender.RENDER.HIGH);
                        if (icon != null) {
                            g2d.drawImage(icon, 0, 0, 64, 64, this);
                        }
                    }

                    {
                        setPreferredSize(new Dimension(64, 64));
                    }
                }, BorderLayout.WEST);

                // tip title:
                add(new JPanel(new BorderLayout(3, 3)) {
                    {
                        setOpaque(false);

                        add(new JLabel("<html><font color=black size=4><b>" + title + "</b></html>") {
                            @Override
                            public void paint(Graphics g) {
                                Graphics2D g2d = (Graphics2D) g;
                                render.setRender(g2d, FoxRender.RENDER.MED);
                                g2d.setColor(new Color(1.0f, 1.0f, 1.0f, 0.2f));
                                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                                super.paint(g);
                            }

                            {
                                setBorder(new EmptyBorder(0, 6, 0, 0));
                            }
                        }, BorderLayout.CENTER);
                        add(new JComponent() {
                            boolean mouseOver = false;

                            {
//										setOpaque(false);
                                setPreferredSize(new Dimension(12, 12));
                                addMouseListener(new MouseAdapter() {
                                    @Override
                                    public void mouseEntered(MouseEvent e) {
                                        mouseOver = true;
                                        repaint();
                                    }

                                    @Override
                                    public void mouseExited(MouseEvent e) {
                                        mouseOver = false;
                                        repaint();
                                    }

                                    @Override
                                    public void mousePressed(MouseEvent e) {
                                        close();
                                    }
                                });
                            }

                            private final Color out = new Color(127, 30, 30);
                            private final Color over = new Color(210, 60, 30);

                            @Override
                            public void paint(Graphics g) {
                                Graphics2D g2d = (Graphics2D) g;
                                render.setRender(g2d, FoxRender.RENDER.MED);
                                g2d.setStroke(new BasicStroke(2f));
                                g2d.setPaint(mouseOver ? over : out);
                                g2d.drawLine(1, 0, getWidth() - 2, 12);
                                g2d.drawLine(getWidth() - 2, 1, 0, 12);
                                g2d.drawRoundRect(0, 0, getWidth() - 1, 12, 9, 9);

                                super.paint(g2d);
                            }
                        }, BorderLayout.EAST);
                    }
                }, BorderLayout.NORTH);

                // the message and footer:
                add(new JLabel() {
                    @Override
                    public void paint(Graphics g) {
                        Graphics2D g2d = (Graphics2D) g;
                        render.setRender(g2d, FoxRender.RENDER.MED);

                        g2d.setColor(new Color(0.0f, 0.0f, 0.0f, 0.35f));
                        g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 6, 6);

                        g2d.setPaint(new GradientPaint(0, 0, baseColor, 0, getHeight() - 11f, secondColor));
                        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);

                        g2d.setColor(new Color(1.0f, 1.0f, 1.0f, 0.3f));
                        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);

                        super.paint(g2d);
                    }

                    {
                        setBorder(new EmptyBorder(-6, 6, 6, 15));
                        setText("<html><h4 font color=black>" + message.replace("\n", "<br>") + "</h4>" +
                                (footer == null ? "" : "<br><hr><font color=white size=2>" + footer)
                        );
                        setVerticalAlignment(TOP);
                        setVerticalTextPosition(TOP);
                        setAlignmentY(TOP_ALIGNMENT);
                    }
                }, BorderLayout.CENTER);

                if (inputField != null) {
                    add(inputField, BorderLayout.SOUTH);
                }
            }

            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                render.setRender(g2d, FoxRender.RENDER.MED);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

                GeneralPath gp = buildForm();
                g2d.setColor(baseColor);
                g2d.fill(gp);

                g2d.setPaint(new GradientPaint(0, 0, secondColor, getWidth() / 3f, 0, baseColor));
                g2d.fill(gp);

                g2d.setPaint(new GradientPaint(0, 0, baseColor, 0, getHeight() - 11f, secondColor));
                g2d.fill(gp);

                g2d.setPaint(borderColor);
                g2d.draw(gp);

                super.paint(g2d);
            }

            private GeneralPath buildForm() {
                GeneralPath gp = new GeneralPath(Path2D.WIND_EVEN_ODD);

                Rectangle rectAim;
                if (aim instanceof JComponent component) {
                    rectAim = component.getBounds();
                } else if (aim instanceof Rectangle rect) {
                    rectAim = rect;
                } else {
                    log.warn("Aim Object has unsupported type: {}", aim.getClass());
                    return gp;
                }

                if (rectAim.y <= getHeight()) {
                    gp.moveTo(5, 5);
                    gp.quadTo(5, 0, 10, 0);
                    gp.lineTo(getWidth() / 2f - 10, 0);

                    gp.lineTo(getWidth() / 2f, -11);
                    gp.lineTo(getWidth() / 2f + 10, 0);

                    gp.lineTo(getWidth() - 12f, 0);
                    gp.quadTo(getWidth() - 6f, 0, getWidth() - 6f, 5);
                    gp.lineTo(getWidth() - 6f, getHeight() - 16f);
                    gp.quadTo(getWidth() - 6f, getHeight() - 11f, getWidth() - 11f, getHeight() - 11f);
                    gp.lineTo(10, getHeight() - 11f);
                    gp.quadTo(5, getHeight() - 11f, 5, getHeight() - 16f);
                    gp.lineTo(5, 5);
                } else {
                    gp.moveTo(5, 5);
                    gp.quadTo(5, 0, 10, 0);
                    gp.lineTo(getWidth() - 11f, 0);
                    gp.quadTo(getWidth() - 6f, 0, getWidth() - 6f, 5);
                    gp.lineTo(getWidth() - 6f, getHeight() - 16f);
                    gp.quadTo(getWidth() - 6f, getHeight() - 11f, getWidth() - 11f, getHeight() - 11f);
                    gp.lineTo(getWidth() / 2f + 10, getHeight() - 11f);

                    gp.lineTo(getWidth() / 2f, getHeight() - 1f);
                    gp.lineTo(getWidth() / 2f - 10, getHeight() - 11f);

                    gp.lineTo(10, getHeight() - 11f);
                    gp.quadTo(5, getHeight() - 11f, 5, getHeight() - 16f);
                    gp.lineTo(5, 5);
                }
                return gp;
            }
        };

        add(contentPanel, BorderLayout.CENTER);
    }

    // настройка расположения всплывающей подсказки здесь:
    public void showTip() {
        try {
            pack();

            Point onScreenPoint = null;
            Rectangle aimRectangle;
            if (aim instanceof JComponent component) {
                aimRectangle = component.getBounds();
                onScreenPoint = component.getLocationOnScreen();
            } else if (aim instanceof Rectangle rect) {
                aimRectangle = rect;
                log.debug("For fullscreen mode only.");
            } else {
                log.warn("Aim Object has unsupported type: {}", aim.getClass());
                return;
            }

            if (aimRectangle.y <= getHeight()) {
                if (onScreenPoint != null) {
                    setLocation(
                            (int) (onScreenPoint.x + aimRectangle.getWidth() / 2D - getWidth() / 2D),
                            (int) (onScreenPoint.y + aimRectangle.getHeight()));
                } else {
                    setLocation(
                            (int) (aimRectangle.x + aimRectangle.getWidth() / 2D - getWidth() / 2D),
                            (int) (aimRectangle.y + aimRectangle.getHeight()));
                }
            } else {
                if (onScreenPoint != null) {
                    setLocation(
                            (int) (onScreenPoint.x + aimRectangle.getWidth() / 2D - getWidth() / 2D),
                            (int) (onScreenPoint.y - aimRectangle.getHeight()));
                } else {
                    // если цель - просто прямоугольник (без компонентов):
                    setLocation(aimRectangle.x + aimRectangle.width / 3, aimRectangle.y);
                }
            }

            setVisible(true);
        } catch (NullPointerException npe) {
            log.error("FoxTip error: " + npe.getMessage());
            throw npe;
        }
    }

    public void close() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        opacity = 1f;
        timer = new Timer(16, e -> {
            opacity -= 0.05f;
            if (opacity <= 0.1f) {
                opacity = 0.1f;
                timer.stop();
                dispose();
            } else {
                contentPanel.repaint();
            }
        });
        timer.start();
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
        if (inputField != null) {
            inputField.requestFocusInWindow();
        }
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        if (this.type != TYPE.INPUT) {
            close();
        }
    }

    @Override
    public void componentShown(ComponentEvent e) {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        opacity = 0.1f;
        timer = new Timer(16, e1 -> {
            opacity += 0.05f;
            if (opacity >= MAX_OPAQUE) {
                opacity = MAX_OPAQUE;
                timer.stop();
            }

            repaint();
        });

        timer.start();
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public enum TYPE {INPUT, INFO}
}
