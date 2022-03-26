package components;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class FOptionPane extends JDialog implements ActionListener {
    public enum TYPE {DEFAULT, YES_NO_TYPE}
    private TYPE type;
    private Thread toThread;
    private JButton OK_BUTTON;
    private JButton NO_BUTTON, YES_BUTTON;
    private BufferedImage ico;
    private int answer = -1, timeout = 20;
    private JLabel timeLastLabel, titleLabel;

    public FOptionPane(String title, String message) {
        this(title, message, null, null, true);
    }

    public FOptionPane(String title, String message, BufferedImage ico) {
        this(title, message, null, ico, true);
    }

    public FOptionPane(String title, String message, TYPE type) {
        this(title, message, type, null, true);
    }

    public FOptionPane(String title, String message, TYPE type, BufferedImage ico, boolean isModal) {
        this.type = type == null ? TYPE.DEFAULT : type;
        if (ico != null) {
            this.ico = ico;
        } else {
            try (InputStream iconStream = FOptionPane.class.getClassLoader().getResourceAsStream("favorite.png")) {
                if (iconStream != null) {
                    this.ico = ImageIO.read(iconStream);
                }
            } catch (IOException e) {
                /* IGNORE WITHOUT ICON */
            }
        }

        setTitle(title);
        setAlwaysOnTop(true);
        setUndecorated(true);
        setPreferredSize(new Dimension(300, 150));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        getRootPane().setBorder(new EmptyBorder(3,3,3,3));

        JPanel basePane = new JPanel(new BorderLayout(3,3)) {
            {
                setBackground(Color.DARK_GRAY);
                setBorder(new EmptyBorder(3,6,3,3));

                JPanel icoPane = new JPanel() {
                    @Override
                    public void paintComponent(Graphics g) {
                        if (FOptionPane.this.ico != null) {
                            g.drawImage(FOptionPane.this.ico,

                                    0, 0,
                                    64, 64,

                                    0, 0,
                                    FOptionPane.this.ico.getWidth(), FOptionPane.this.ico.getHeight(),

                                    this);
                        }
                    }

                    {
                        setPreferredSize(new Dimension(64, 0));
                    }
                };

                JPanel upLabelPane = new JPanel(new BorderLayout(3,3)) {
                    {
                        setOpaque(false);

                        titleLabel = new JLabel(FOptionPane.this.getTitle()) {
                            {
                                setForeground(Color.WHITE);
                            }
                        };
                        timeLastLabel = new JLabel() {
                            {
                                setForeground(Color.GRAY);
                            }
                        };

                        add(titleLabel, BorderLayout.WEST);
                        add(timeLastLabel, BorderLayout.EAST);
                    }
                };

                JPanel mesPane = new JPanel(new BorderLayout(3, 3)) {
                    {
                        setOpaque(false);
                        setBorder(new EmptyBorder(16,6,0,0));

                        JTextArea mesArea = new JTextArea() {
                            {
                                setEditable(false);
                                setForeground(Color.WHITE);
                                setText(message);
                                setWrapStyleWord(true);
                                setLineWrap(true);
                                setBorder(null);
                                setBackground(Color.DARK_GRAY);
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

                JPanel btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 3)) {
                    {
                        setOpaque(false);

                        switch (FOptionPane.this.type) {
                            case DEFAULT -> {
                                OK_BUTTON = new JButton("OK") {
                                    {
                                        setActionCommand("ok");
                                        addActionListener(FOptionPane.this);
                                    }
                                };
                                add(OK_BUTTON);
                            }
                            case YES_NO_TYPE -> {
                                YES_BUTTON = new JButton("Да") {
                                    {
                                        setActionCommand("yes");
                                        addActionListener(FOptionPane.this);
                                    }
                                };
                                NO_BUTTON = new JButton("Нет") {
                                    {
                                        setActionCommand("no");
                                        addActionListener(FOptionPane.this);
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
                add(icoPane, BorderLayout.WEST);
                add(mesPane, BorderLayout.CENTER);
                add(btnPane, BorderLayout.SOUTH);
            }
        };

        add(basePane);

        toThread = new Thread(() -> {
            while (timeout > 0) {
                timeout--;
                timeLastLabel.setText("Осталось: " + timeout + " сек.");
                try {TimeUnit.MILLISECONDS.sleep(1000);
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
