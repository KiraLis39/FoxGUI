package fox.components;

import fox.FoxFontBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class FoxConsole2 extends JDialog implements KeyListener {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final FoxFontBuilder fontBuilder = new FoxFontBuilder();

    private FoxConsole2 consoleFrame;
    private KeyListener kList;
    private JFrame parentFrame;
    private JPanel upTactAndClockPane;
    private JScrollPane consoleScroll;
    private JTextArea consoleArea;
    private JTextField inputArea;
    private JLabel oClock;
    private boolean clockIsOn;

    public FoxConsole2(JFrame parent, String consoleTitle, Boolean isModal) {
        this(parent, consoleTitle, isModal, null);
    }

    public FoxConsole2(JFrame parent, String consoleTitle, Boolean isModal, KeyListener _kList) {
        super(parent, consoleTitle, isModal);

        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception e) {
            log.error("Couldn't get specified look and feel, for some reason.");
        }

        consoleFrame = this;
        parentFrame = parent;

        kList = Objects.requireNonNullElse(_kList, this);

        setLayout(new BorderLayout());
        setModalExclusionType(Dialog.ModalExclusionType.NO_EXCLUDE);
        setUndecorated(true);
        setVisible(false);

        JComponent testFrameComponent = (JComponent) parentFrame.getContentPane();
        testFrameComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_BACK_QUOTE, 0),
                "onoff"
        );
        testFrameComponent.getActionMap().put("onoff", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.debug(">> " + e.getID() + " - " + e.getModifiers());
                visibleChanger();
            }
        });

        createNewConsole();
    }

    private void visibleChanger() {
        oClock.setText("" + dateFormat.format(System.currentTimeMillis()));

        consoleFrame.setSize(new Dimension(parentFrame.getWidth(), parentFrame.getHeight() / 3 * 2));
        consoleFrame.setLocation(parentFrame.getX(), parentFrame.getY());

        consoleFrame.setVisible(!consoleFrame.isVisible());
    }

    private void createNewConsole() {
        consoleFrame.setOpacity(0.85f);

        upTactAndClockPane = new JPanel() {
            {
                setBackground(new Color(0.0f, 0.0f, 0.5f, 0.75f));
                setBorder(new EmptyBorder(3, 10, 5, 10));
            }
        };

        oClock = new JLabel();
        oClock.setText("" + dateFormat.format(System.currentTimeMillis()));
        oClock.setFont(fontBuilder.setFoxFont(FoxFontBuilder.FONT.ARIAL_NARROW, 14, false));
        oClock.setForeground(Color.GRAY.brighter());

        upTactAndClockPane.add(oClock);
        if (!clockIsOn) {
            upTactAndClockPane.setVisible(false);
        }

        consoleArea = new JTextArea() {
            {
                setBackground(Color.BLACK);
                setForeground(Color.GREEN);
                setFont(fontBuilder.setFoxFont(FoxFontBuilder.FONT.ARIAL_NARROW, 14, false));
                setBorder(new EmptyBorder(5, 5, 5, 5));
                setEditable(false);
                setFocusable(true);
                setLineWrap(true);
                setWrapStyleWord(true);
                setText("***CONSOLE OUT*** \n");
            }
        };

        consoleScroll = new JScrollPane(consoleArea) {
            {
                setAutoscrolls(true);
                setWheelScrollingEnabled(true);
                setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            }
        };

        inputArea = new JTextField() {
            {
                setBackground(Color.BLACK);
                setForeground(Color.ORANGE);
                setFont(fontBuilder.setFoxFont(FoxFontBuilder.FONT.ARIAL_NARROW, 14, false));
                setBorder(new EmptyBorder(5, 5, 5, 5));
                setEditable(true);
                setFocusable(true);
                setAutoRequestFocus(true);

                addKeyListener(kList);
            }
        };

        JPanel foxConsolePanel = new JPanel(new BorderLayout()) {
            {
                setBackground(new Color(0.0f, 0.0f, 0.0f, 0.5f));
                setForeground(Color.GREEN);
                setBorder(new EmptyBorder(3, 5, 3, 5));

                add(upTactAndClockPane, BorderLayout.NORTH);
                add(consoleScroll, BorderLayout.CENTER);
                add(inputArea, BorderLayout.SOUTH);

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        inputArea.requestFocus();
                        inputArea.grabFocus();
                        oClock.setText("" + dateFormat.format(System.currentTimeMillis()));
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                    }
                });
            }
        };

        consoleFrame.add(foxConsolePanel);

        consoleFrame.setMinimumSize(new Dimension(parentFrame.getWidth() / 2, parentFrame.getHeight() / 3 * 2));
        consoleFrame.setLocation(parentFrame.getX(), parentFrame.getY());
    }


    public void setTextareaBackgroundColor(Color color) {
        consoleArea.setBackground(color);
    }

    public void setTextareaForegroundColor(Color color) {
        consoleArea.setForeground(color);
    }

    public void setInputareaBackgroundColor(Color color) {
        inputArea.setBackground(color);
    }

    public void setInputareaForegroundColor(Color color) {
        inputArea.setForeground(color);
    }

    public void setClockPanelVisible(Boolean visible) {
        upTactAndClockPane.setVisible(visible);
    }

    public void setConsoleClockText(String time) {
        oClock.setText(time);
    }

    public void setClockIsOn(Boolean clockIsOn) {
        this.clockIsOn = clockIsOn;
    }

    public void setConsoleClockFont(Font font) {
        oClock.setFont(font);
    }

    public void setConsoleClockBackground(Color color) {
        upTactAndClockPane.setBackground(color);
    }

    public void setConsoleAreaFont(Font newFont) {
        consoleArea.setFont(newFont);
    }

    public void appendToConsole(String string) {
        consoleArea.append("\n" + string);
    }

    public void setText(String str) {
        consoleArea.setText(str);
    }

    public void clear() {
        consoleArea.setText("");
    }

    public void changeInputAreaText(String text) {
        if (text == null) {
            inputArea.setText("");
            return;
        }

        inputArea.setText(text);
    }

    public void setFocusInArea() {
        inputArea.setRequestFocusEnabled(true);
        inputArea.requestFocus();
    }

    @Override
    public void keyPressed(KeyEvent key) {
        if (key.getKeyCode() == KeyEvent.VK_ENTER && !inputArea.getText().isEmpty()) {
            consoleArea.append("\n" + inputArea.getText());
            consoleArea.setText(consoleArea.getText().trim());
            consoleArea.setCaretPosition(consoleArea.getText().length());

            inputArea.setText("");
            inputArea.requestFocus();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_QUOTE) {
            if (consoleFrame.isVisible()) {
                consoleFrame.dispose();
            } else {
                setFocusInArea();
            }
        }
    }

    public void keyTyped(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_QUOTE) {
            if (consoleFrame.isVisible()) {
                consoleFrame.dispose();
            } else {
                setFocusInArea();
            }
        }
    }
}
