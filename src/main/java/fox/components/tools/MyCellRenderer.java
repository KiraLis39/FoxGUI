package fox.components.tools;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

public class MyCellRenderer extends JPanel implements ListCellRenderer<JButton> {
    private static int cellHeight;

    public MyCellRenderer(int cellHeight) {
        MyCellRenderer.cellHeight = cellHeight;

        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        // setFont(registry.Registry.trackSelectedFont);
        JButton label = new JButton() {
            {
                setHorizontalTextPosition(JButton.RIGHT);
                setHorizontalAlignment(JButton.LEFT);
//                setFont(registry.Registry.trackSelectedFont);
            }
        };

        add(label, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends JButton> list, JButton value, int index, boolean isSelected, boolean cellHasFocus) {
        setEnabled(list.isEnabled());
        setFont(list.getFont());

//        BufferedImage ico = ((ShareTableRow) value).getImIcon();
//        if (ico.getWidth() >= cellHeight || ico.getHeight() >= cellHeight) {
//            BufferedImage tmp = new BufferedImage(cellHeight - 8, cellHeight - 8, BufferedImage.TYPE_INT_ARGB);
//            Graphics g = tmp.getGraphics();
//            g.drawImage(ico, 0,0,tmp.getWidth(), tmp.getHeight(), null);
//            g.dispose();
//            ico = tmp;
//        }
//        label.setIcon(new ImageIcon(ico));
//        label.setText("<html><b>[" + ((ShareTableRow) value).getCount() + "]</b> " + ((ShareTableRow) value).getText());

//        label.setFont(registry.Registry.trackSelectedFont);

//        if (isSelected) {
//            if (((ListRow) value).getOwner().isFallTrack(index)) {
//                label.setBackground(Color.PINK);
//                label.setForeground(Color.BLACK);
//            } else {
//                label.setBackground(Color.GRAY);
//                label.setForeground(Color.BLACK);
//
//                int ind = ((CustomList) list).getPlayedRowIndex();
//                if (((ListRow) value).getCount() - 1 == ind) {
//                    label.setBackground(Color.BLACK);
//                    label.setForeground(Color.CYAN);
//                }
//            }
//
//        } else if (((ListRow) value).getOwner().isAlarmSounded()) {
//            label.setBackground(Color.ORANGE.darker());
//            label.setForeground(Color.WHITE);
//        } else if (((ListRow) value).getOwner().isFallTrack(index)) {
//            label.setBackground(Color.RED.darker());
//            label.setForeground(Color.WHITE);
//        } else {
//            label.setBackground(list.getBackground());
//            label.setForeground(Color.WHITE);
//            label.setFont(null);
//
//            int ind = ((CustomList)list).getPlayedRowIndex();
//            if (((ListRow) value).getCount() - 1 == ind) {
//                label.setBackground(Color.BLACK);
//                label.setForeground(Color.CYAN);
//            } else {
//                setBackground(list.getBackground());
//            }
//        }

        return this;
    }
}
