package com.dev.ui.components;

import com.dev.util.UIResources;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class ButtonFactory {
    
    private static final int DEFAULT_WIDTH = 100;
    private static final int DEFAULT_HEIGHT = 35;
    
    private ButtonFactory() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static JButton createStyledButton(String text, Color bgColor) {
        return createStyledButton(text, bgColor, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    public static JButton createStyledButton(String text, Color bgColor, int width, int height) {
        JButton button = new JButton(text);
        configureButton(button, bgColor, width, height);
        addHoverEffect(button, bgColor);
        return button;
    }
    
    private static void configureButton(JButton button, Color bgColor, int width, int height) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(UIResources.getCustomFont(Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(width, height));
    }
    
    private static void addHoverEffect(JButton button, Color originalColor) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(originalColor.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent evt) {
                button.setBackground(originalColor);
            }
        });
    }
}
