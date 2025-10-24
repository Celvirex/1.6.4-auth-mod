package com.example.authmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("authmod.json");
    
    public DatabaseConfig database = new DatabaseConfig();
    public SecurityConfig security = new SecurityConfig();
    public MessageConfig messages = new MessageConfig();
    
    public static class DatabaseConfig {
        public String host = "localhost";
        public int port = 3306;
        public String database = "minecraft_auth";
        public String username = "minecraft";
        public String password = "password";
        public int connectionPoolSize = 5;
        public int maxLifetime = 1800000; // 30 minutes in milliseconds
    }
    
    public static class SecurityConfig {
        public int minPasswordLength = 6;
        public int maxLoginAttempts = 3;
        public int sessionTimeoutMinutes = 30;
        public boolean requireRegistration = true;
        public boolean preventMovement = true;
        public boolean preventChat = true;
        public boolean preventInteraction = true;
        public boolean preventDamage = true;
    }
    
    public static class MessageConfig {
        public String notAuthenticated = "§cPlease authenticate using /login <password> or /register <password> <confirmPassword>";
        public String registerSuccess = "§aRegistration successful! You are now logged in.";
        public String loginSuccess = "§aLogin successful!";
        public String invalidPassword = "§cInvalid password!";
        public String alreadyRegistered = "§cYou are already registered!";
        public String passwordsDontMatch = "§cPasswords do not match!";
        public String passwordTooShort = "§cPassword must be at least %d characters long!";
        public String changePasswordSuccess = "§aPassword changed successfully!";
        public String changePasswordFail = "§cOld password is incorrect!";
    }
    
    public static ModConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = new FileReader(CONFIG_PATH.toFile())) {
                return GSON.fromJson(reader, ModConfig.class);
            } catch (IOException e) {
                System.err.println("Failed to load AuthMod config, using defaults: " + e.getMessage());
            }
        }
        
        // Create default config
        ModConfig defaultConfig = new ModConfig();
        defaultConfig.save();
        return defaultConfig;
    }
    
    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save AuthMod config: " + e.getMessage());
        }
    }
    
    public String getDatabaseUrl() {
        return String.format("jdbc:mysql://%s:%d/%s", 
            database.host, database.port, database.database);
    }
}
