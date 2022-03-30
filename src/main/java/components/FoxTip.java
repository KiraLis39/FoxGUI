package components;

import render.FoxRender;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

public class FoxTip extends JDialog implements WindowFocusListener, ComponentListener {
    public enum TYPE {INPUT, INFO}
    private TYPE type;
    private Container owner;

    private JPanel contentPanel;
    private JTextField inputField;

    private Timer timer;

    private float opacity = 0.1f;
    private final float MAX_OPAQUE = 0.9f;

    private Color baseColor = new Color(1.0f, 1.0f, 1.0f, 0.2f);
    private Color secondColor = new Color(0.25f, 0.25f, 0.35f, 0.9f);
    private Color borderColor = new Color(0.5f, 0.5f, 0.5f, 1.0f);


    public FoxTip(Container owner, TYPE type, BufferedImage icon, String title, String message, String footer) {
        new FoxTip(owner, type, icon, title, message, footer, baseColor, secondColor, borderColor);
    }

    public FoxTip(Container owner, TYPE type, BufferedImage icon, String title, String message, String footer,
                  Color baseColor, Color secondColor, Color borderColor) {
        this.type = type;
        this.owner = owner;
        this.baseColor = baseColor;
        this.secondColor = secondColor;
        this.borderColor = borderColor;
        this.inputField = type == TYPE.INPUT ? new JTextField() : null;

        setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setAlwaysOnTop(true);
        setModal(false);

        addWindowFocusListener(this);
        addComponentListener(this);

        contentPanel = new JPanel(new BorderLayout(4, 4)) {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                FoxRender.setRender(g2d, FoxRender.RENDER.HIGH);
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
                gp.moveTo(5, 5);
                gp.quadTo(5, 0, 10, 0);
                gp.lineTo(getWidth() - 11, 0);
                gp.quadTo(getWidth() - 6, 0, getWidth() - 6, 5);
                gp.lineTo(getWidth() - 6, getHeight() - 16);
                gp.quadTo(getWidth() - 6, getHeight() - 11, getWidth() - 11, getHeight() - 11);
                gp.lineTo(getWidth() / 2 + 10, getHeight() - 11);
                gp.lineTo(getWidth() / 2, getHeight() - 1);
                gp.lineTo(getWidth() / 2 - 10, getHeight() - 11);
                gp.lineTo(10, getHeight() - 11);
                gp.quadTo(5, getHeight() - 11, 5, getHeight() - 16);
                gp.lineTo(5, 5);
                return gp;
            }

            {
                setOpaque(false);
                setBorder(new EmptyBorder(6, 10, 15, 10));
                setAlignmentY(JLabel.TOP_ALIGNMENT);

                add(new JLabel() {
                    @Override
                    public void paintComponent(Graphics g) {
                        Graphics2D g2d = (Graphics2D) g;
                        FoxRender.setRender(g2d, FoxRender.RENDER.HIGH);
                        if (icon != null) {
                            g2d.drawImage(icon, 0, 0, 64, 64, this);
                        }
                    }

                    {
                        setPreferredSize(new Dimension(64, 64));
                    }
                }, BorderLayout.WEST);

                add(new JPanel(new BorderLayout(3, 3)) {
                    {
                        setOpaque(false);

                        add(new JLabel("<html><font color=black size=4><b>" + title + "</b>") {
                            @Override
                            public void paint(Graphics g) {
                                Graphics2D g2d = (Graphics2D) g;
                                FoxRender.setRender(g2d, FoxRender.RENDER.HIGH);
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
                                    public void mouseEntered(MouseEvent e) {
                                        mouseOver = true;
                                        repaint();
                                    }

                                    public void mouseExited(MouseEvent e) {
                                        mouseOver = false;
                                        repaint();
                                    }

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
                                FoxRender.setRender(g2d, FoxRender.RENDER.HIGH);
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

                add(new JLabel() {
                    @Override
                    public void paint(Graphics g) {
                        Graphics2D g2d = (Graphics2D) g;
                        FoxRender.setRender(g2d, FoxRender.RENDER.HIGH);
                        g2d.setPaint(new GradientPaint(0, 0, baseColor, 0, getHeight() - 11, secondColor));
                        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                        g2d.setColor(new Color(1.0f, 1.0f, 1.0f, 0.3f));
                        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                        super.paint(g2d);
                    }

                    {
                        setBorder(new EmptyBorder(-6, 6, 0, 15));
                        setText("<html><h4 font color=black>" + message + "</font>" +
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
        };

        add(contentPanel, BorderLayout.CENTER);
    }

    public void showTip() {
        pack();
        setLocation(
                owner.getLocationOnScreen().x + owner.getWidth() / 2 - getWidth() / 2,
                owner.getLocationOnScreen().y - getHeight());
        setVisible(true);
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
    public void componentHidden(ComponentEvent e) {}
    public void componentResized(ComponentEvent e) {}
    public void componentMoved(ComponentEvent e) {}
}
