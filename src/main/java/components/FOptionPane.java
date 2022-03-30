package components;

import utils.FoxFontBuilder;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class FOptionPane extends JDialog implements ActionListener {
    public enum TYPE {DEFAULT, YES_NO_TYPE}
    private final TYPE type;

    private Thread toThread;
    private JButton OK_BUTTON, NO_BUTTON, YES_BUTTON;
    private BufferedImage ico;
    private int answer = -1, timeout = 20;
    private JLabel titleLabel;
    private JPanel upLabelPane;
    private String timeLastLabel;

    public FOptionPane(String title, String message) {
        this(title, message, null, null, true);
    }

    public FOptionPane(String title, String message, BufferedImage ico) {
        this(title, message, null, ico, true);
    }

    public FOptionPane(String title, String message, TYPE type) {
        this(title, message, type, null, true);
    }

    public FOptionPane(String title, String message, TYPE type, BufferedImage _ico, boolean isModal) {
        this.type = type == null ? TYPE.DEFAULT : type;
        if (ico != null) {
            this.ico = _ico;
        } else {
            try (InputStream iconStream = FOptionPane.class.getClassLoader().getResourceAsStream("favorite.png")) {
                if (iconStream != null) {
                    this.ico = ImageIO.read(iconStream);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        setTitle(title);
        setAlwaysOnTop(true);
        setUndecorated(true);
        setBackground(new Color(0.1f, 0.1f, 0.1f, 0.9f));
        setPreferredSize(new Dimension(300, 150));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        getRootPane().setBorder(new EmptyBorder(3, 3, 3, 3));

        JPanel basePane = new JPanel(new BorderLayout(3, 3)) {
            {
                setOpaque(false);

                upLabelPane = new JPanel(new BorderLayout(0, 0)) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.2f));
                        g.fillRect(0, 0, getWidth(), getHeight());

                        g.setColor(Color.GRAY);
                        g.drawString(timeLastLabel, (int) (getWidth() - FoxFontBuilder.getStringBounds(g, timeLastLabel).getWidth()), 14);
                    }

                    {
                        setOpaque(false);
                        setBorder(new EmptyBorder(0, 6, 3, 3));

                        titleLabel = new JLabel(FOptionPane.this.getTitle()) {
                            {
                                setForeground(Color.WHITE);
                                setBackground(new Color(0, 0, 0, 0));
                            }
                        };

                        add(titleLabel, BorderLayout.WEST);
                    }
                };

                JPanel midContantPane = new JPanel(new BorderLayout(0, 0)) {
                    {
                        setOpaque(false);
                        setBorder(new EmptyBorder(3, 9, 0, 0));

                        JPanel icoPane = new JPanel(new BorderLayout(0, 0)) {
                            @Override
                            public void paintComponent(Graphics g) {
                                if (ico != null) {
                                    g.drawImage(ico,

                                            0, 8,
                                            64, 72,

                                            0, 0,
                                            ico.getWidth(), ico.getHeight(),

                                            this);
                                } else {
                                    g.setColor(Color.MAGENTA);
                                    g.fillRect(0, 0, getWidth(), getHeight());
                                }
                            }

                            {
                                setPreferredSize(new Dimension(64, 64));
                            }
                        };

                        JPanel mesPane = new JPanel(new BorderLayout(0, 0)) {
                            {
                                setOpaque(false);
                                setBorder(new EmptyBorder(16, 6, 0, 0));

                                JTextArea mesArea = new JTextArea() {
                                    {
                                        setOpaque(false);
                                        setEditable(false);
                                        setForeground(Color.WHITE);
                                        setText(message);
                                        setWrapStyleWord(true);
                                        setLineWrap(true);
                                        setBorder(null);
                                    }
                                };

                                JScrollPane mesScroll = new JScrollPane(mesArea) {
                                    {
                                        setOpaque(false);
                                        getViewport().setOpaque(false);
                                        setBorder(null);
                                        getViewport().setBorder(null);
                                    }
                                };

                                add(mesScroll);
                            }
                        };

                        add(icoPane, BorderLayout.WEST);
                        add(mesPane, BorderLayout.CENTER);
                    }
                };

                JPanel btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 3)) {
                    {
                        setOpaque(false);

                        switch (FOptionPane.this.type) {
                            case DEFAULT -> {
                                OK_BUTTON = new JButton("OK") {
                                    {
                                        setActionCommand("ok");
                                        setFocusPainted(false);
                                        addActionListener(FOptionPane.this);
                                        setBackground(Color.DARK_GRAY);
                                        setForeground(Color.WHITE);
                                    }
                                };
                                add(OK_BUTTON);
                            }
                            case YES_NO_TYPE -> {
                                YES_BUTTON = new JButton("Да") {
                                    {
                                        setActionCommand("yes");
                                        addActionListener(FOptionPane.this);
                                        setFocusPainted(false);
                                        setBackground(Color.DARK_GRAY);
                                        setForeground(Color.WHITE);
                                    }
                                };
                                NO_BUTTON = new JButton("Нет") {
                                    {
                                        setActionCommand("no");
                                        addActionListener(FOptionPane.this);
                                        setFocusPainted(false);
                                        setBackground(Color.DARK_GRAY);
                                        setForeground(Color.WHITE);
                                    }
                                };
                                add(YES_BUTTON);
                                add(NO_BUTTON);
                            }
                            default -> {
                            }
                        }
                    }
                };

                add(upLabelPane, BorderLayout.NORTH);
                add(midContantPane, BorderLayout.CENTER);
                add(btnPane, BorderLayout.SOUTH);
            }
        };

        add(basePane);

        toThread = new Thread(() -> {
            while (timeout > 0) {
                timeout--;
                timeLastLabel = "Осталось: " + timeout + " сек.";
                upLabelPane.repaint();
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException ignore) {
                }
            }
            answer = 1;
            FOptionPane.this.dispose();
        });
        toThread.start();

        pack();
        setLocationRelativeTo(null);

        setModal(isModal);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setModalExclusionType(ModalExclusionType.NO_EXCLUDE);
        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2D = (Graphics2D) g;

        g2D.setColor(Color.DARK_GRAY);
        g2D.setStroke(new BasicStroke(2));
        g2D.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
    }

    public int get() {
        int result = answer;
        timeout = 0;
        return result;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "yes" -> answer = 0;
            case "no" -> answer = 1;
            default -> answer = -2;
        }

        FOptionPane.this.dispose();
    }
}
