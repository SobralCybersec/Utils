package com.dev.ui;

import com.dev.service.LLMOrganizerService;
import com.dev.util.ConfigManager;
import com.dev.util.UIResources;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;

public class APIConfigDialog extends JDialog {
    
    private JComboBox<String> providerCombo;
    private JTextField apiKeyField;
    private JTextField endpointField;
    private JTextArea instructionsArea;
    private boolean saved = false;
    
    public APIConfigDialog(Frame parent) {
        super(parent, "Configurar IA (LLM)", true);
        setSize(600, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        initComponents();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Painel de seleção de provedor
        JPanel providerPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        providerPanel.setBorder(BorderFactory.createTitledBorder("Provedor de IA"));
        
        providerPanel.add(new JLabel("Provedor:"));
        providerCombo = new JComboBox<>(new String[]{
            "LOCAL (Sem IA - Análise de Padrões)",
            "OpenAI (GPT-3.5/4)",
            "Google Gemini",
            "Customizado (gpt4free, etc)"
        });
        providerCombo.addActionListener(e -> updateInstructions());
        providerPanel.add(providerCombo);
        
        providerPanel.add(new JLabel("API Key:"));
        apiKeyField = new JTextField();
        apiKeyField.setToolTipText("Deixe em branco se não necessário");
        providerPanel.add(apiKeyField);
        
        providerPanel.add(new JLabel("Endpoint (Custom):"));
        endpointField = new JTextField();
        endpointField.setToolTipText("URL do endpoint customizado");
        endpointField.setEnabled(false);
        providerPanel.add(endpointField);
        
        // Área de instruções
        instructionsArea = new JTextArea();
        instructionsArea.setEditable(false);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        instructionsArea.setBackground(new Color(240, 240, 240));
        instructionsArea.setFont(UIResources.getCustomFont(Font.PLAIN, 12));
        instructionsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(instructionsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Instruções"));
        scrollPane.setPreferredSize(new Dimension(0, 200));
        
        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton testButton = new JButton("Testar Conexão");
        testButton.addActionListener(e -> testConnection());
        
        JButton saveButton = new JButton("Salvar");
        saveButton.addActionListener(e -> saveConfig());
        
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());
        
        JButton openEnvButton = new JButton("Abrir .env");
        openEnvButton.addActionListener(e -> openEnvFile());
        
        buttonPanel.add(openEnvButton);
        buttonPanel.add(testButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(providerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        updateInstructions();
        loadCurrentConfig();
    }
    
    private void updateInstructions() {
        int index = providerCombo.getSelectedIndex();
        
        endpointField.setEnabled(index == 3);
        apiKeyField.setEnabled(index != 0);
        
        String instructions = switch (index) {
            case 0 -> """
                MODO LOCAL (Sem IA)
                
                Usa analise de padroes locais sem conexao com internet.
                Detecta automaticamente:
                - Projetos (arquivos de codigo)
                - Documentos de trabalho
                - Midia pessoal
                - Downloads e backups
                - Configuracoes e logs
                
                Nao requer API Key ou configuracao adicional.
                """;
            case 1 -> """
                OPENAI (GPT-3.5/4)
                
                1. Crie uma conta em: https://platform.openai.com
                2. Gere uma API Key em: https://platform.openai.com/api-keys
                3. Cole a chave no campo "API Key"
                
                Modelos disponiveis:
                - gpt-3.5-turbo (mais rapido e barato)
                - gpt-4 (mais preciso, mais caro)
                
                Custo aproximado: $0.002 por 1000 arquivos
                """;
            case 2 -> """
                GOOGLE GEMINI
                
                1. Acesse: https://makersuite.google.com/app/apikey
                2. Crie uma API Key gratuita
                3. Cole a chave no campo "API Key"
                
                Modelo: gemini-pro
                
                Limite gratuito: 60 requisicoes por minuto
                Ideal para uso pessoal!
                """;
            case 3 -> """
                CUSTOMIZADO (gpt4free)
                
                Use APIs gratuitas ou endpoints customizados.
                
                Exemplo com gpt4free:
                1. Clone: https://github.com/xtekky/gpt4free
                2. Execute o servidor local
                3. Configure o endpoint: http://localhost:1337/v1/chat/completions
                
                Ou use qualquer endpoint compativel com OpenAI API.
                
                Deixe API Key em branco se nao necessario.
                """;
            default -> "";
        };
        
        instructionsArea.setText(instructions);
    }
    
    private void loadCurrentConfig() {
        String provider = ConfigManager.get("AI_PROVIDER", "LOCAL");
        
        switch (provider) {
            case "LOCAL" -> providerCombo.setSelectedIndex(0);
            case "OPENAI" -> {
                providerCombo.setSelectedIndex(1);
                apiKeyField.setText(ConfigManager.get("OPENAI_API_KEY", ""));
            }
            case "GEMINI" -> {
                providerCombo.setSelectedIndex(2);
                apiKeyField.setText(ConfigManager.get("GEMINI_API_KEY", ""));
            }
            case "CUSTOM" -> {
                providerCombo.setSelectedIndex(3);
                apiKeyField.setText(ConfigManager.get("CUSTOM_API_KEY", ""));
                endpointField.setText(ConfigManager.get("CUSTOM_ENDPOINT", ""));
            }
        }
    }
    
    private void testConnection() {
        saveConfig();
        
        JOptionPane.showMessageDialog(this,
            "Teste de conexao nao implementado.\n" +
            "A conexao sera testada na primeira organizacao.",
            "Teste",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void saveConfig() {
        int index = providerCombo.getSelectedIndex();
        
        String provider = switch (index) {
            case 0 -> "LOCAL";
            case 1 -> "OPENAI";
            case 2 -> "GEMINI";
            case 3 -> "CUSTOM";
            default -> "LOCAL";
        };
        
        String apiKey = apiKeyField.getText().trim();
        String endpoint = endpointField.getText().trim();
        
        LLMOrganizerService.configure(provider, apiKey, endpoint);
        
        saved = true;
        
        String configPath = ConfigManager.getConfigPath().toString();
        
        JOptionPane.showMessageDialog(this,
            "Configuracao salva com sucesso!\n" +
            "Provedor: " + providerCombo.getSelectedItem() + "\n" +
            "Arquivo: " + configPath,
            "Sucesso",
            JOptionPane.INFORMATION_MESSAGE);
        
        dispose();
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    private void openEnvFile() {
        try {
            java.nio.file.Path configPath = ConfigManager.getConfigPath();
            
            if (!Files.exists(configPath)) {
                ConfigManager.createTemplateEnv();
                int result = JOptionPane.showConfirmDialog(this,
                    "Arquivo .env nao encontrado.\n" +
                    "Um template foi criado em: " + configPath + "\n\n" +
                    "Deseja abrir a pasta?",
                    "Arquivo nao encontrado",
                    JOptionPane.YES_NO_OPTION);
                
                if (result == JOptionPane.YES_OPTION) {
                    Desktop.getDesktop().open(configPath.getParent().toFile());
                }
            } else {
                Desktop.getDesktop().open(configPath.toFile());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao abrir arquivo: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
