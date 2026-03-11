package com.dev.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public final class UIResources {
    
    private static final String FONT_PATH = "/fonts/custom.ttf";
    private static final String IMAGE_PATH = "/images/background.png";
    private static final String FALLBACK_FONT = "Segoe UI";
    private static final int FALLBACK_FONT_SIZE = 12;
    
    private static Font customFont;
    private static ImageIcon backgroundImage;
    
    private UIResources() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static Font getCustomFont(int style, float size) {
        if (customFont == null) {
            customFont = loadFont();
        }
        return customFont.deriveFont(style, size);
    }
    
    public static ImageIcon getBackgroundImage() {
        if (backgroundImage == null) {
            backgroundImage = loadImage();
        }
        return backgroundImage;
    }
    
    public static JPanel createBackgroundPanel() {
        return new BackgroundPanel();
    }
    
    private static Font loadFont() {
        try (InputStream is = UIResources.class.getResourceAsStream(FONT_PATH)) {
            if (is != null) {
                Font font = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
                return font;
            }
        } catch (Exception e) {
            System.err.println("Failed to load custom font: " + e.getMessage());
        }
        return new Font(FALLBACK_FONT, Font.PLAIN, FALLBACK_FONT_SIZE);
    }
    
    private static ImageIcon loadImage() {
        try (InputStream is = UIResources.class.getResourceAsStream(IMAGE_PATH)) {
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                return new ImageIcon(img);
            }
        } catch (Exception e) {
            System.err.println("Failed to load background image: " + e.getMessage());
        }
        return null;
    }
    
    private static class BackgroundPanel extends JPanel {
        private final transient Image bgImage;
        
        BackgroundPanel() {
            ImageIcon icon = getBackgroundImage();
            this.bgImage = (icon != null) ? icon.getImage() : null;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImage != null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}
