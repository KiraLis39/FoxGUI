package components.tools;

import javax.swing.*;
import java.awt.*;

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
