package components;

import java.awt.Container;
import java.awt.Dimension;
import javax.swing.ViewportLayout;


/*
 * Its a layout for JScrollPanes. It helps arrange the text correctly
 * by panel resizes. Should be..
 * 
 * Used as myScrollPane.getViewport().setLayout(new ConstrainedViewPortLayout());
 */

@SuppressWarnings("serial")
public class ConstrainedViewPortLayout extends ViewportLayout {
	
	@Override
    public Dimension preferredLayoutSize(Container parent) {
        Dimension preferredViewSize = super.preferredLayoutSize(parent);

        Container viewportContainer = parent.getParent();
        if (viewportContainer != null) {
            Dimension parentSize = viewportContainer.getSize();
            preferredViewSize.height = parentSize.height;
        }

        return preferredViewSize;
    }
}
