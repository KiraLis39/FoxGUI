package fox.render.foxLFui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicLookAndFeel;


public class FoxLookAndFeel extends BasicLookAndFeel {
    @Override
    public String getName() {
        return "FoxLookAndFeel";
    }

    @Override
    public String getID() {
        return getName();
    }

    @Override
    public String getDescription() {
        return "Fox l&f style theme";
    }

    @Override
    public boolean isNativeLookAndFeel() {
        return false;
    }

    @Override
    public boolean isSupportedLookAndFeel() {
        return true;
    }


    @Override
    protected void initClassDefaults(UIDefaults table) {
        // По прежнему оставляем дефолтную инициализацию, так как мы пока что не реализовали все
        // различные UI-классы для J-компонентов
        super.initClassDefaults(table);

//        // Label
//        table.put ( "LabelUI", ... );
//        table.put ( "ToolTipUI", ... );
//
//        // Button
        table.put("ButtonUI", MyButtonUI.class.getCanonicalName());
//        table.put ( "ToggleButtonUI", ... );
//        table.put ( "CheckBoxUI", ... );
//        table.put ( "RadioButtonUI", ... );
//
//        // Menu
//        table.put ( "MenuBarUI", ... );
//        table.put ( "MenuUI", ... );
//        table.put ( "PopupMenuUI", ... );
//        table.put ( "MenuItemUI", ... );
//        table.put ( "CheckBoxMenuItemUI", ... );
//        table.put ( "RadioButtonMenuItemUI", ... );
//        table.put ( "PopupMenuSeparatorUI", ... );
//
//        // Scroll
//        table.put ( "ScrollBarUI", ... );
//        table.put ( "ScrollPaneUI", ... );
//
//        // Text
//        table.put ( "TextFieldUI", ... );
//        table.put ( "PasswordFieldUI", ... );
//        table.put ( "FormattedTextFieldUI", ... );
//        table.put ( "TextAreaUI", ... );
//        table.put ( "EditorPaneUI", ... );
//        table.put ( "TextPaneUI", ... );
//
//        // Toolbar
        table.put("ToolBarUI", MyToolBarUI.class.getCanonicalName());
//        table.put ( "ToolBarSeparatorUI", ... );
//
//        // Table
//        table.put ( "TableUI", ... );
//        table.put ( "TableHeaderUI", ... );
//
//        // Chooser
//        table.put ( "ColorChooserUI", ... );
//        table.put ( "FileChooserUI", ... );
//
//        // Container
//        table.put ( "PanelUI", ... );
//        table.put ( "ViewportUI", ... );
//        table.put ( "RootPaneUI", ... );
//        table.put ( "TabbedPaneUI", ... );
//        table.put ( "SplitPaneUI", ... );
//
//        // Complex fox.components
//        table.put ( "ProgressBarUI", ... );
//        table.put ( "SliderUI", ... );
//        table.put ( "SpinnerUI", ... );
//        table.put ( "TreeUI", ... );
//        table.put ( "ListUI", ... );
//        table.put ( "ComboBoxUI", ... );
//
//        // Desktop pane
//        table.put ( "DesktopPaneUI", ... );
//        table.put ( "DesktopIconUI", ... );
//        table.put ( "InternalFrameUI", ... );
//
//        // Option pane
//        table.put ( "OptionPaneUI", ... );
    }
}
