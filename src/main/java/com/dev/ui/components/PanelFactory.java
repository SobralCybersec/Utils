package com.dev.ui.components;

import com.dev.ui.theme.ColorPalette;
import com.dev.util.UIResources;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public final class PanelFactory {
    
    private static final int BORDER_THICKNESS = 2;
    
    private PanelFactory() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static JPanel createTitledPanel(String title, Color borderColor) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setBorder(createTitledBorder(title, borderColor));
        return panel;
    }
    
    public static TitledBorder createTitledBorder(String title, Color titleColor) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(titleColor, BORDER_THICKNESS),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            UIResources.getCustomFont(Font.BOLD, 14),
            titleColor
        );
    }
}
