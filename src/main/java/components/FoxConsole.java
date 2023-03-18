package components;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import utils.InputAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;

@Data
@Slf4j
@Component
@RequiredArgsConstructor
public class FoxConsole extends JDialog {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final InputAction inputAction;

    private JFrame parentFrame;
    private JPanel upTactAndClockPane, foxConsolePanel;
    private JScrollPane consoleScroll;
    private JTextArea consoleArea;
    private JTextField inputArea;
    private JLabel oClock;
    private Font f0, f1, f2;
    private boolean clockIsOn = false;


    public void buildFoxConsole(JFrame parent) {
        buildFoxConsole(parent, "Console", true);
    }

    public void buildFoxConsole(JFrame parent, String consoleTitle, boolean isModal) {
//        super(parent);
        setTitle(consoleTitle);
        setModal(isModal);
        this.parentFrame = parent;

        setLayout(new BorderLayout());
        setModalExclusionType(Dialog.ModalExclusionType.NO_EXCLUDE);
        setUndecorated(true);
        setVisible(false);

        createNewConsole();

        inAc();
    }

    private void inAc() {
        inputAction.add("console", FoxConsole.this);
        inputAction.set("console", "onOff", KeyEvent.VK_BACK_QUOTE, 0, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                visibleChanger();
            }
        });

        inputAction.add("parent", parentFrame);
        inputAction.set("parent", "onOff", KeyEvent.VK_BACK_QUOTE, 0, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                visibleChanger();
            }
        });

        inputAction.add("inputArea", inputArea);
        inputAction.set("inputArea", "send", KeyEvent.VK_ENTER, 0, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!inputArea.getText().isEmpty()) {
                    consoleArea.append("\n" + inputArea.getText());
                    consoleArea.setText(consoleArea.getText().trim());
                    consoleArea.setCaretPosition(consoleArea.getText().length());

                    inputArea.setText("");
                    inputArea.requestFocus();
                }
            }
        });
    }

    void visibleChanger() {
        oClock.setText("" + dateFormat.format(System.currentTimeMillis()));

        setSize(new Dimension(parentFrame.getWidth(), parentFrame.getHeight() / 3 * 2));
        setLocation(parentFrame.getX(), parentFrame.getY());
        setVisible(!isVisible());
    }

    private void createNewConsole() {
        setOpacity(0.85f);

        upTactAndClockPane = new JPanel() {
            {
                setBackground(new Color(0.0f, 0.0f, 0.5f, 0.75f));
                setBorder(new EmptyBorder(3, 10, 5, 10));
            }
        };

        oClock = new JLabel();
        oClock.setText("" + dateFormat.format(System.currentTimeMillis()));
        if (f0 != null) {
            oClock.setFont(f0);
        }
        oClock.setForeground(Color.GRAY.brighter());

        upTactAndClockPane.add(oClock);
        if (!clockIsOn) {
            upTactAndClockPane.setVisible(false);
        }

        consoleArea = new JTextArea() {
            {
                setBackground(Color.BLACK);
                setForeground(Color.GREEN);
                if (f1 != null) {
                    setFont(f1);
                }
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
                if (f2 != null) {
                    setFont(f2);
                }
                setBorder(new EmptyBorder(5, 5, 5, 5));
                setEditable(true);
                setFocusable(true);
                setAutoRequestFocus(true);
            }
        };

        foxConsolePanel = new JPanel(new BorderLayout()) {
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
                });
            }
        };

        add(foxConsolePanel);

        setMinimumSize(new Dimension(parentFrame.getWidth() / 2, parentFrame.getHeight() / 3 * 2));
        setLocation(parentFrame.getX(), parentFrame.getY());
    }

    public void setClockVisible(Boolean onOff) {
        upTactAndClockPane.setVisible(onOff);
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

    public void setConsoleClockText(String time) {
        oClock.setText(time);
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

    public void setClockFont(Font f0) {
        this.f0 = f0;
    }

    public void setOutputAreaFont(Font f1) {
        this.f1 = f1;
    }

    public void setInputAreaFont(Font f2) {
        this.f2 = f2;
    }
}
