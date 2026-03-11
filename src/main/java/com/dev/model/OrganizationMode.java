package com.dev.model;

public enum OrganizationMode {
    BY_TYPE("Por Tipo de Arquivo"),
    BY_EXTENSION("Por Extensao"),
    ALPHABETICAL("Ordem Alfabetica (A-Z)"),
    BY_DATE("Por Data de Modificacao"),
    BY_SIZE("Por Tamanho"),
    INTELLIGENT("Organizacao Inteligente (IA)"),
    BY_PROGRAM("Por Programa/Projeto (Recursivo + IA)");
    
    private final String displayName;
    
    OrganizationMode(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
