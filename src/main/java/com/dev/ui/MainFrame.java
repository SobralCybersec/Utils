package com.dev.ui;

import com.dev.util.ConfigManager;
import com.dev.util.UIResources;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    
    private static final String TITLE = "Utilidades Diárias";
    private static final int DEFAULT_WIDTH = 900;
    private static final int DEFAULT_HEIGHT = 650;
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 600;
    
    public MainFrame() {
        configureFrame();
        initComponents();
    }
    
    private void configureFrame() {
        setTitle(TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        add(createHeader(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }
    
    private JPanel createMainPanel() {
        JPanel backgroundPanel = UIResources.createBackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.add(new FileOrganizerPanel(), BorderLayout.CENTER);
        return backgroundPanel;
    }
    
    private JPanel createHeader() {
        HeaderPanel header = new HeaderPanel();
        return header.build();
    }
    
    private JPanel createFooter() {
        FooterPanel footer = new FooterPanel();
        return footer.build();
    }
    
    public static void main(String[] args) {
        configureSystemLookAndFeel();
        ConfigManager.createTemplateEnv();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
    
    private static void configureSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static class HeaderPanel {
        private static final Color BACKGROUND_COLOR = new Color(41, 128, 185);
        private static final Color SUBTITLE_COLOR = new Color(236, 240, 241);
        private static final int HEIGHT = 70;
        
        public JPanel build() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(BACKGROUND_COLOR);
            panel.setPreferredSize(new Dimension(0, HEIGHT));
            panel.add(createTextPanel(), BorderLayout.CENTER);
            return panel;
        }
        
        private JPanel createTextPanel() {
            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);
            textPanel.add(createTitleLabel());
            textPanel.add(createSubtitleLabel());
            return textPanel;
        }
        
        private JLabel createTitleLabel() {
            JLabel title = new JLabel("Utils Preguiçosas", SwingConstants.CENTER);
            title.setFont(UIResources.getCustomFont(Font.BOLD, 28));
            title.setForeground(Color.WHITE);
            return title;
        }
        
        private JLabel createSubtitleLabel() {
            JLabel subtitle = new JLabel("Organização", SwingConstants.CENTER);
            subtitle.setFont(UIResources.getCustomFont(Font.PLAIN, 14));
            subtitle.setForeground(SUBTITLE_COLOR);
            return subtitle;
        }
    }
    
    private static class FooterPanel {
        private static final Color BACKGROUND_COLOR = new Color(52, 73, 94);
        private static final Color TEXT_COLOR = new Color(189, 195, 199);
        private static final int HEIGHT = 30;
        
        public JPanel build() {
            JPanel panel = new JPanel();
            panel.setBackground(BACKGROUND_COLOR);
            panel.setPreferredSize(new Dimension(0, HEIGHT));
            panel.add(createFooterLabel());
            return panel;
        }
        
        private JLabel createFooterLabel() {
            JLabel footer = new JLabel("Utils 1.0");
            footer.setForeground(TEXT_COLOR);
            footer.setFont(UIResources.getCustomFont(Font.PLAIN, 11));
            return footer;
        }
    }
}
