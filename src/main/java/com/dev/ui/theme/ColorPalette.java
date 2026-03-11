package com.dev.ui.theme;

import java.awt.*;

public final class ColorPalette {
    
    public static final Color PRIMARY = new Color(52, 152, 219);
    public static final Color SUCCESS = new Color(46, 204, 113);
    public static final Color DANGER = new Color(231, 76, 60);
    public static final Color WARNING = new Color(230, 126, 34);
    public static final Color INFO = new Color(142, 68, 173);
    public static final Color SECONDARY = new Color(149, 165, 166);
    
    public static final Color HEADER_BG = new Color(41, 128, 185);
    public static final Color FOOTER_BG = new Color(52, 73, 94);
    public static final Color BACKGROUND = new Color(236, 240, 241);
    
    public static final Color TEXT_PRIMARY = new Color(52, 73, 94);
    public static final Color TEXT_SECONDARY = new Color(189, 195, 199);
    public static final Color TEXT_LIGHT = new Color(236, 240, 241);
    
    public static final Color LOG_BG = new Color(44, 62, 80);
    public static final Color LOG_TEXT = new Color(236, 240, 241);
    
    public static final Color BORDER = new Color(189, 195, 199);
    
    private ColorPalette() {
        throw new UnsupportedOperationException("Utility class");
    }
}
