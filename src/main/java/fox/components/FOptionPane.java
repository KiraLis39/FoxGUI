package fox.components;

import fox.FoxFontBuilder;
import fox.FoxRender;
import fox.utils.InputAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

@Slf4j
@RequiredArgsConstructor
public class FOptionPane extends JDialog implements ActionListener, MouseListener, MouseMotionListener {
    private static final FoxFontBuilder fontBuilder = new FoxFontBuilder();
    private static final FoxRender render = new FoxRender();
    private final transient InputAction inputAction = new InputAction();
    private final Color baseground = new Color(0.1f, 0.1f, 0.1f, 0.9f);
    private TYPE type;
    private JButton OK_BUTTON, NO_BUTTON, YES_BUTTON;
    private Object answer;
    private AtomicInteger timeout;
    private JLabel titleLabel;
    private JTextField inputField;
    private JPanel upLabelPane;
    private String timeLastLabel;
    private transient BufferedImage ico;
    private Point p;
    private Point oldLocation;

    public FOptionPane buildFOptionPane(String title, String message) {
        return buildFOptionPane(title, message, null, null, null, null, true);
    }

    public FOptionPane buildFOptionPane(String title, String message, Integer timeout, boolean isModal) {
        return buildFOptionPane(title, message, null, null, null, timeout, isModal);
    }

    public FOptionPane buildFOptionPane(String title, String message, BufferedImage ico, boolean isModal) {
        return buildFOptionPane(title, message, null, ico, null, null, isModal);
    }

    public FOptionPane buildFOptionPane(String title, String message, BufferedImage ico, int timeout, boolean isModal) {
        return buildFOptionPane(title, message, null, ico, null, timeout, isModal);
    }

    public FOptionPane buildFOptionPane(String title, String message, TYPE type, Cursor cursor) {
        return buildFOptionPane(title, message, type, null, cursor, null, true);
    }

    public FOptionPane buildFOptionPane(String title, String message, TYPE type, Cursor cursor, int timeout, boolean isModal) {
        return buildFOptionPane(title, message, type, null, cursor, timeout, isModal);
    }

    public FOptionPane buildFOptionPane(
            String title,
            String message,
            TYPE _type,
            BufferedImage _ico,
            Cursor cursor,
            Integer _timeout,
            boolean isModal
    ) {
        type = _type == null ? TYPE.INFO : _type;
        if (ico != null) {
            this.ico = _ico;
        } else {
            try (InputStream iconStream = getClass().getResourceAsStream("/images/favorite.png")) {
                if (iconStream != null) {
                    this.ico = ImageIO.read(iconStream);
                }
            } catch (Exception e) {
                log.warn("FOption`s ico read exception: {}", e.getMessage());
            }
        }

        if (cursor != null) {
            setCursor(cursor);
        }
        setTitle(title);
        setFocusable(false);
        setUndecorated(true);
        setBackground(baseground);
        setPreferredSize(new Dimension(300, 150));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        getRootPane().setBorder(new EmptyBorder(3, 3, 3, 3));

        addMouseListener(this);
        addMouseMotionListener(this);

        JPanel basePane = new JPanel(new BorderLayout(3, 3)) {
            {
                setOpaque(false);
                setFocusable(false);

                upLabelPane = new JPanel(new BorderLayout(0, 0)) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.2f));
                        g.fillRect(0, 0, getWidth(), getHeight());

                        if (timeLastLabel != null) {
                            g.setColor(Color.GRAY);
                            g.drawString(timeLastLabel, (int) (getWidth() - fontBuilder.getStringBounds(g, timeLastLabel).getWidth()), 14);
                        }
                    }

                    {
                        setOpaque(false);
                        setFocusable(false);
                        setBorder(new EmptyBorder(0, 6, 3, 3));

                        titleLabel = new JLabel(getTitle()) {
                            {
                                setFocusable(false);
                                setForeground(Color.WHITE);
                                setBackground(new Color(0, 0, 0, 0));
                            }
                        };

                        add(titleLabel, BorderLayout.WEST);
                    }
                };

                JPanel midContentPane = new JPanel(new BorderLayout(0, 0)) {
                    {
                        setOpaque(false);
                        setFocusable(false);
                        setBorder(new EmptyBorder(3, 9, 0, 0));

                        JPanel icoPane = new JPanel(new BorderLayout(0, 0)) {
                            @Override
                            public void paintComponent(Graphics g) {
                                if (ico != null) {
                                    g.drawImage(ico,

                                            0, 8,
                                            64, 64,

                                            0, 0,
                                            ico.getWidth(), ico.getHeight(),

                                            this);
                                } else {
                                    g.setColor(Color.MAGENTA);
                                    g.fillRect(0, 0, 64, 64);
                                }
                            }

                            {
                                setFocusable(false);
                                setPreferredSize(new Dimension(64, 64));
                            }
                        };

                        JPanel mesPane = new JPanel(new BorderLayout(1, 1)) {
                            {
                                setOpaque(false);
                                setFocusable(false);
                                setIgnoreRepaint(true);
                                setDoubleBuffered(false);
                                setBackground(baseground);
                                setBorder(new EmptyBorder(8, 4, 0, 2));

                                if (type == TYPE.INPUT) {
                                    JPanel inputPane = new JPanel(new GridLayout(3, 1, 0, 0)) {
                                        {
                                            setOpaque(false);
                                            setFocusable(false);
                                            setBorder(new EmptyBorder(0, 0, 0, 9));

                                            inputField = new JTextField() {{
                                                setFocusable(true);
                                                setRequestFocusEnabled(true);
                                                setAutoRequestFocus(true);
                                            }};
                                            add(new JLabel(message) {
                                                {
                                                    setFocusable(false);
                                                    setForeground(Color.WHITE);
                                                }
                                            });
                                            add(inputField);
                                        }
                                    };

                                    add(inputPane, BorderLayout.CENTER);
                                } else {
                                    JTextArea mesArea = new JTextArea() {
                                        {
                                            setOpaque(false);
                                            setForeground(Color.WHITE);
                                            setBackground(baseground);
                                            setEditable(false);
                                            setText(message);
                                            setWrapStyleWord(true);
                                            setLineWrap(true);
                                            setBorder(new EmptyBorder(1, 6, 0, 0));
                                        }
                                    };

                                    JScrollPane mesScroll = new JScrollPane(mesArea) {
                                        {
                                            setOpaque(false);
                                            getViewport().setOpaque(false);
                                            setBorder(null);
                                            getViewport().setBorder(null);
                                            setBackground(baseground);
                                        }
                                    };

                                    add(mesScroll);
                                }
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
                            case INPUT, INFO -> {
                                OK_BUTTON = new JButton("OK") {
                                    {
                                        addActionListener(FOptionPane.this);
                                        setActionCommand("ok");
                                        setFocusPainted(false);
                                        setBackground(Color.DARK_GRAY);
                                        setForeground(Color.WHITE);
                                    }
                                };
                                add(OK_BUTTON);
                            }
                            case YES_NO_TYPE -> {
                                YES_BUTTON = new JButton("Да") {
                                    {
                                        addActionListener(FOptionPane.this);
                                        setActionCommand("yes");
                                        setFocusPainted(false);
                                        setBackground(Color.DARK_GRAY);
                                        setForeground(Color.WHITE);
                                    }
                                };
                                NO_BUTTON = new JButton("Нет") {
                                    {
                                        addActionListener(FOptionPane.this);
                                        setActionCommand("no");
                                        setFocusPainted(false);
                                        setBackground(Color.DARK_GRAY);
                                        setForeground(Color.WHITE);
                                    }
                                };
                                add(YES_BUTTON);
                                add(NO_BUTTON);
                            }
                            case VARIANTS -> {
                                YES_BUTTON = new JButton("Выйти из игры") {
                                    {
                                        addActionListener(FOptionPane.this);
                                        setActionCommand("yes");
                                        setFocusPainted(false);
                                        setBackground(Color.DARK_GRAY);
                                        setForeground(Color.WHITE);
                                    }
                                };
                                NO_BUTTON = new JButton("Сохранение и загрузка") {
                                    {
                                        addActionListener(FOptionPane.this);
                                        setActionCommand("no");
                                        setFocusPainted(false);
                                        setBackground(Color.DARK_GRAY);
                                        setForeground(Color.WHITE);
                                    }
                                };
                                add(YES_BUTTON);
                                add(NO_BUTTON);
                            }
                        }
                    }
                };

                add(upLabelPane, BorderLayout.NORTH);
                add(midContentPane, BorderLayout.CENTER);
                add(btnPane, BorderLayout.SOUTH);
            }
        };

        add(basePane);

        inputAction.add("foxPane", this.getRootPane());
        inputAction.set(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "foxPane", "abort",
                KeyEvent.VK_ESCAPE, 0, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        timeout.set(0);
                    }
                });

        pack();
        setLocationRelativeTo(null);
        setModalExclusionType(ModalExclusionType.NO_EXCLUDE);

        new Thread(() -> {
            timeout = new AtomicInteger(_timeout == null ? 15 : Math.max(1, _timeout));

            while (_timeout == null || timeout.get() > 0) {
                if (_timeout != null && _timeout > 0) {
                    timeLastLabel = "Осталось: " + timeout + " сек.";
                }
                upLabelPane.repaint();
                try {
                    sleep(499);
                    if (_timeout != null && _timeout > 0 && timeout.decrementAndGet() == 0) {
                        break;
                    } else {
                        sleep(499);
                    }
                    Thread.yield();
                } catch (InterruptedException e) {
                    log.warn("FOption`s thread was interrupted: {}", e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
            answer = -1;
            stop();
        }).start();

        if (type == TYPE.INPUT) {
            inputField.requestFocusInWindow();
        }

        setAlwaysOnTop(true);
        setModal(isModal);
        setVisible(true);

        return this;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2D = (Graphics2D) g;
        render.setRender(g2D, FoxRender.RENDER.MED);

        g2D.setColor(Color.DARK_GRAY);
        g2D.setStroke(new BasicStroke(2));
        g2D.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
    }

    public void stop() {
        log.debug("Stop the FOption...");
        timeout.set(0);
        setModal(false);
        setVisible(false);
        FOptionPane.this.dispose();
    }

    public Object get() {
        timeout.set(0);
        if (type == TYPE.INPUT) {
            return inputField.getText();
        } else {
            return answer;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (type.equals(TYPE.INPUT)) {
            answer = inputField.getText();
        } else {
            switch (e.getActionCommand()) {
                case "yes" -> answer = 0;
                case "no" -> answer = 1;
                default -> answer = -2;
            }
        }

        log.info("Dispose FOption with answer: {}", answer);
        stop();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        p = MouseInfo.getPointerInfo().getLocation();
        oldLocation = getLocation();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point newPoint = MouseInfo.getPointerInfo().getLocation();
        setLocation(
                oldLocation.x - (p.x - newPoint.x),
                oldLocation.y - (p.y - newPoint.y));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        oldLocation = getLocation();
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public enum TYPE {INFO, YES_NO_TYPE, VARIANTS, INPUT}
}
