package com.dev.util;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigManager {
    
    private static final String CONFIG_FILE = ".env";
    private static final Path CONFIG_PATH = Paths.get(System.getProperty("user.home"), ".fileorganizer", CONFIG_FILE);
    private static final Properties properties = new Properties();
    
    static {
        loadConfig();
    }
    
    public static void loadConfig() {
        try {
            // Tenta carregar do diretório do usuário primeiro
            if (Files.exists(CONFIG_PATH)) {
                try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
                    properties.load(input);
                    System.out.println("Config loaded from: " + CONFIG_PATH);
                }
            } else {
                // Tenta carregar do diretório atual
                Path localConfig = Paths.get(CONFIG_FILE);
                if (Files.exists(localConfig)) {
                    try (InputStream input = Files.newInputStream(localConfig)) {
                        properties.load(input);
                        System.out.println("Config loaded from: " + localConfig);
                    }
                } else {
                    System.out.println("No .env file found, using defaults");
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load config: " + e.getMessage());
        }
    }
    
    public static void saveConfig(Map<String, String> config) {
        try {
            // Cria diretório se não existir
            Files.createDirectories(CONFIG_PATH.getParent());
            
            // Atualiza properties
            config.forEach(properties::setProperty);
            
            // Salva no arquivo
            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(output, "File Organizer Configuration");
                System.out.println("Config saved to: " + CONFIG_PATH);
            }
            
            // Também salva uma cópia no diretório atual para facilitar
            Path localConfig = Paths.get(CONFIG_FILE);
            try (OutputStream output = Files.newOutputStream(localConfig)) {
                properties.store(output, "File Organizer Configuration");
            }
            
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }
    
    public static String get(String key) {
        return properties.getProperty(key);
    }
    
    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public static void set(String key, String value) {
        properties.setProperty(key, value);
    }
    
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    
    public static Map<String, String> getAllConfig() {
        Map<String, String> config = new HashMap<>();
        properties.forEach((key, value) -> config.put(key.toString(), value.toString()));
        return config;
    }
    
    public static void createTemplateEnv() {
        try {
            Path templatePath = Paths.get(".env.example");
            
            if (!Files.exists(templatePath)) {
                String template = """
                    # File Organizer - Configuração de IA
                    # Copie este arquivo para .env e configure suas chaves
                    
                    # Provedor de IA: LOCAL, OPENAI, GEMINI, CUSTOM
                    AI_PROVIDER=LOCAL
                    
                    # OpenAI Configuration
                    # Obtenha em: https://platform.openai.com/api-keys
                    OPENAI_API_KEY=
                    OPENAI_MODEL=gpt-3.5-turbo
                    
                    # Google Gemini Configuration
                    # Obtenha em: https://makersuite.google.com/app/apikey
                    GEMINI_API_KEY=
                    
                    # Custom Endpoint (gpt4free, Ollama, etc)
                    CUSTOM_ENDPOINT=http://localhost:1337/v1/chat/completions
                    CUSTOM_API_KEY=
                    
                    # Configurações Gerais
                    AUTO_BACKUP=false
                    LOG_LEVEL=INFO
                    """;
                
                Files.writeString(templatePath, template);
                System.out.println(".env.example created");
            }
        } catch (IOException e) {
            System.err.println("Failed to create template: " + e.getMessage());
        }
    }
    
    public static Path getConfigPath() {
        return CONFIG_PATH;
    }
}
