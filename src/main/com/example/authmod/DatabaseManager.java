package com.example.authmod;

import java.sql.*;
import java.util.UUID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class DatabaseManager {
    private Connection connection;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    
    public DatabaseManager() {
        // Configure your database connection here
        this.host = "localhost";
        this.port = 3306;
        this.database = "authmod";
        this.username = "minecraft";
        this.password = "password";
    }
    
    public void initializeDatabase() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        connection = DriverManager.getConnection(url, username, password);
        
        // Create players table if it doesn't exist
        String createTableSQL = "CREATE TABLE IF NOT EXISTS players (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "username VARCHAR(16) NOT NULL, " +
                "password_hash VARCHAR(255) NOT NULL, " +
                "salt VARCHAR(255) NOT NULL, " +
                "registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "last_login TIMESTAMP NULL)";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
        }
    }
    
    public boolean registerPlayer(UUID playerId, String username, String password) throws SQLException {
        if (isPlayerRegistered(playerId)) {
            return false;
        }
        
        String salt = generateSalt();
        String passwordHash = hashPassword(password, salt);
        
        String sql = "INSERT INTO players (id, username, password_hash, salt) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.setString(2, username);
            stmt.setString(3, passwordHash);
            stmt.setString(4, salt);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean authenticatePlayer(UUID playerId, String password) throws SQLException {
        String sql = "SELECT password_hash, salt FROM players WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String salt = rs.getString("salt");
                String inputHash = hashPassword(password, salt);
                return storedHash.equals(inputHash);
            }
            return false;
        }
    }
    
    public boolean changePassword(UUID playerId, String oldPassword, String newPassword) throws SQLException {
        if (!authenticatePlayer(playerId, oldPassword)) {
            return false;
        }
        
        String salt = generateSalt();
        String newPasswordHash = hashPassword(newPassword, salt);
        
        String sql = "UPDATE players SET password_hash = ?, salt = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setString(2, salt);
            stmt.setString(3, playerId.toString());
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean isPlayerRegistered(UUID playerId) throws SQLException {
        String sql = "SELECT 1 FROM players WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }
    
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String combined = password + salt;
            byte[] hashedBytes = md.digest(combined.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
