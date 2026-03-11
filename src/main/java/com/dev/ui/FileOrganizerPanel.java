package com.dev.ui;

import com.dev.model.OrganizationMode;
import com.dev.service.AdvancedOrganizerService;
import com.dev.service.IntelligentAnalyzer;
import com.dev.service.LLMOrganizerService;
import com.dev.ui.components.ButtonFactory;
import com.dev.ui.components.PanelFactory;
import com.dev.ui.theme.ColorPalette;
import com.dev.util.UIResources;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileOrganizerPanel extends JPanel {
    
    private static final int PANEL_PADDING = 15;
    private static final int COMPONENT_SPACING = 10;
    private static final int FIELD_HEIGHT = 35;
    private static final int BUTTON_WIDTH_SMALL = 100;
    private static final int BUTTON_WIDTH_LARGE = 220;
    private static final int BUTTON_HEIGHT_SMALL = 35;
    private static final int BUTTON_HEIGHT_LARGE = 45;
    
    private final JTextField pathField;
    private final DefaultListModel<String> listModel;
    private final JTextArea logArea;
    private final JButton organizeButton;
    private final JButton analyzeButton;
    private final JComboBox<OrganizationMode> modeComboBox;
    private final List<Path> selectedPaths;
    
    public FileOrganizerPanel() {
        this.selectedPaths = new ArrayList<>();
        this.listModel = new DefaultListModel<>();
        this.pathField = new JTextField();
        this.logArea = new JTextArea();
        this.organizeButton = ButtonFactory.createStyledButton("Organizar Arquivos", ColorPalette.DANGER, BUTTON_WIDTH_LARGE, BUTTON_HEIGHT_LARGE);
        this.analyzeButton = ButtonFactory.createStyledButton("Analisar", ColorPalette.INFO);
        this.modeComboBox = new JComboBox<>(OrganizationMode.values());
        
        configurePanel();
        initComponents();
    }
    
    private void configurePanel() {
        setLayout(new BorderLayout(COMPONENT_SPACING, COMPONENT_SPACING));
        setBorder(new EmptyBorder(PANEL_PADDING, PANEL_PADDING, PANEL_PADDING, PANEL_PADDING));
        
        if (UIResources.getBackgroundImage() == null) {
            setBackground(ColorPalette.BACKGROUND);
        } else {
            setOpaque(false);
        }
    }
    
    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(COMPONENT_SPACING, COMPONENT_SPACING));
        topPanel.setOpaque(false);
        topPanel.add(new FolderSelectionPanel(), BorderLayout.NORTH);
        topPanel.add(new OrganizationModePanel(), BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(new ActionButtonPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, COMPONENT_SPACING, 0));
        panel.setOpaque(false);
        panel.add(new FolderListPanel());
        panel.add(new LogPanel());
        return panel;
    }
    
    private void selectFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            pathField.setText(path);
            log("Pasta selecionada: " + path);
        }
    }
    
    private void scanFolder() {
        String pathStr = pathField.getText().trim();
        
        if (pathStr.isEmpty()) {
            showWarning("Selecione uma pasta primeiro!");
            return;
        }
        
        try {
            Path basePath = Paths.get(pathStr);
            listModel.clear();
            selectedPaths.clear();
            listModel.addElement(basePath.toString());
            selectedPaths.add(basePath);
            
            enableButtons(true);
            log("Pasta carregada: " + basePath);
            log("Pronto para organizar!");
        } catch (Exception e) {
            log("ERRO: " + e.getMessage());
            showError("Erro ao selecionar pasta: " + e.getMessage());
        }
    }
    
    private void analyzeFolder() {
        if (selectedPaths.isEmpty()) {
            showWarning("Selecione uma pasta primeiro!");
            return;
        }
        
        log("\n=== ANALISE INTELIGENTE ===");
        
        new SwingWorker<Map<String, Integer>, String>() {
            @Override
            protected Map<String, Integer> doInBackground() {
                Path folder = selectedPaths.get(0);
                publish("Analisando arquivos em: " + folder.getFileName());
                return IntelligentAnalyzer.analyzeFolder(folder);
            }
            
            @Override
            protected void process(List<String> chunks) {
                chunks.forEach(FileOrganizerPanel.this::log);
            }
            
            @Override
            protected void done() {
                try {
                    Map<String, Integer> stats = get();
                    log("\nEstatisticas encontradas:");
                    stats.forEach((category, count) -> {
                        String folderName = IntelligentAnalyzer.getIntelligentFolderName(category);
                        log("  " + folderName + ": " + count + " arquivo(s)");
                    });
                    log("\nAnalise concluida!\n");
                } catch (Exception e) {
                    log("Erro na analise: " + e.getMessage());
                }
            }
        }.execute();
    }
    
    private void organizeFiles() {
        if (selectedPaths.isEmpty()) {
            showWarning("Selecione uma pasta primeiro!");
            return;
        }
        
        OrganizationMode mode = (OrganizationMode) modeComboBox.getSelectedItem();
        enableButtons(false);
        log("\n=== INICIANDO ORGANIZACAO ===");
        log("Modo: " + mode.getDisplayName());
        
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() {
                try {
                    AdvancedOrganizerService.organize(selectedPaths, mode);
                    publish("\nOrganizacao concluida com sucesso!");
                } catch (Exception e) {
                    publish("\nERRO: " + e.getMessage());
                }
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                chunks.forEach(FileOrganizerPanel.this::log);
            }
            
            @Override
            protected void done() {
                log("=== FINALIZADO ===\n");
                enableButtons(true);
                showInfo("Organizacao finalizada com sucesso!");
            }
        }.execute();
    }
    
    private void clearAll() {
        pathField.setText("");
        listModel.clear();
        selectedPaths.clear();
        logArea.setText("");
        enableButtons(false);
        log("Interface limpa. Pronto para nova operacao.");
    }
    
    private void openAPIConfig() {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        APIConfigDialog dialog = new APIConfigDialog(parentFrame);
        dialog.setVisible(true);
        
        if (dialog.isSaved()) {
            log("IA configurada: " + LLMOrganizerService.getProvider());
        }
    }
    
    private void enableButtons(boolean enabled) {
        organizeButton.setEnabled(enabled);
        analyzeButton.setEnabled(enabled);
    }
    
    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    
    private class FolderSelectionPanel extends JPanel {
        
        FolderSelectionPanel() {
            setLayout(new BorderLayout(COMPONENT_SPACING, COMPONENT_SPACING));
            setOpaque(false);
            setBorder(PanelFactory.createTitledBorder("Selecao de Pasta", ColorPalette.TEXT_LIGHT));
            
            pathField.setFont(UIResources.getCustomFont(Font.PLAIN, 13));
            pathField.setPreferredSize(new Dimension(0, FIELD_HEIGHT));
            
            add(pathField, BorderLayout.CENTER);
            add(createButtonPanel(), BorderLayout.EAST);
        }
        
        private JPanel createButtonPanel() {
            JPanel panel = new JPanel(new GridLayout(1, 2, COMPONENT_SPACING, 0));
            panel.setOpaque(false);
            
            JButton browseButton = ButtonFactory.createStyledButton("Procurar", ColorPalette.PRIMARY);
            browseButton.addActionListener(e -> selectFolder());
            
            JButton scanButton = ButtonFactory.createStyledButton("Carregar", ColorPalette.SUCCESS);
            scanButton.addActionListener(e -> scanFolder());
            
            panel.add(browseButton);
            panel.add(scanButton);
            return panel;
        }
    }
    
    private class OrganizationModePanel extends JPanel {
        
        OrganizationModePanel() {
            setLayout(new BorderLayout(COMPONENT_SPACING, COMPONENT_SPACING));
            setOpaque(false);
            setBorder(PanelFactory.createTitledBorder("Modo de Organizacao", ColorPalette.TEXT_LIGHT));

            configureModeComboBox();
            add(modeComboBox, BorderLayout.CENTER);
            add(createButtonPanel(), BorderLayout.EAST);
        }
        
        private void configureModeComboBox() {
            modeComboBox.setFont(UIResources.getCustomFont(Font.PLAIN, 13));
            modeComboBox.setPreferredSize(new Dimension(0, FIELD_HEIGHT));
            modeComboBox.setSelectedItem(OrganizationMode.BY_PROGRAM);
            modeComboBox.setRenderer(new ModeComboBoxRenderer());
        }
        
        private JPanel createButtonPanel() {
            JPanel panel = new JPanel(new GridLayout(1, 2, COMPONENT_SPACING, 0));
            panel.setOpaque(false);
            
            analyzeButton.addActionListener(e -> analyzeFolder());
            analyzeButton.setEnabled(false);
            
            JButton configButton = ButtonFactory.createStyledButton("Config IA", ColorPalette.PRIMARY);
            configButton.addActionListener(e -> openAPIConfig());
            configButton.setToolTipText("Configurar IA (OpenAI, Gemini, etc)");
            
            panel.add(analyzeButton);
            panel.add(configButton);
            return panel;
        }
    }
    
    private class FolderListPanel extends JPanel {
        
        FolderListPanel() {
            setLayout(new BorderLayout(5, 5));
            setOpaque(false);
            setBorder(PanelFactory.createTitledBorder("Pasta Selecionada", ColorPalette.TEXT_LIGHT));
            
            JList<String> folderList = new JList<>(listModel);
            folderList.setFont(UIResources.getCustomFont(Font.PLAIN, 12));
            folderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            folderList.setBackground(Color.WHITE);
            
            JScrollPane scrollPane = new JScrollPane(folderList);
            scrollPane.setBorder(BorderFactory.createLineBorder(ColorPalette.BORDER));
            
            add(scrollPane, BorderLayout.CENTER);
        }
    }
    
    private class LogPanel extends JPanel {
        
        LogPanel() {
            setLayout(new BorderLayout(5, 5));
            setOpaque(false);
            setBorder(PanelFactory.createTitledBorder("Log de Operacoes", ColorPalette.TEXT_LIGHT));
            
            logArea.setFont(UIResources.getCustomFont(Font.PLAIN, 11));
            logArea.setEditable(false);
            logArea.setBackground(ColorPalette.LOG_BG);
            logArea.setForeground(ColorPalette.LOG_TEXT);
            logArea.setLineWrap(true);
            logArea.setWrapStyleWord(true);
            
            JScrollPane scrollPane = new JScrollPane(logArea);
            scrollPane.setBorder(BorderFactory.createLineBorder(ColorPalette.BORDER));
            
            add(scrollPane, BorderLayout.CENTER);
        }
    }
    
    private class ActionButtonPanel extends JPanel {
        
        ActionButtonPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
            setOpaque(false);
            
            organizeButton.setFont(UIResources.getCustomFont(Font.BOLD, 16));
            organizeButton.addActionListener(e -> organizeFiles());
            organizeButton.setEnabled(false);
            
            JButton clearButton = ButtonFactory.createStyledButton("Limpar", ColorPalette.SECONDARY, 120, BUTTON_HEIGHT_LARGE);
            clearButton.setFont(UIResources.getCustomFont(Font.BOLD, 14));
            clearButton.addActionListener(e -> clearAll());
            
            add(organizeButton);
            add(clearButton);
        }
    }
    
    private static class ModeComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == OrganizationMode.BY_PROGRAM) {
                setFont(getFont().deriveFont(Font.BOLD));
                setForeground(ColorPalette.DANGER);
            }
            return this;
        }
    }
}
