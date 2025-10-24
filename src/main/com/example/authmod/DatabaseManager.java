public class DatabaseManager {
    private Connection connection;
    private final ModConfig config;
    
    public DatabaseManager(ModConfig config) {
        this.config = config;
    }
    
    public void initializeDatabase() throws SQLException {
        String url = config.getDatabaseUrl();
        connection = DriverManager.getConnection(url, config.database.username, config.database.password);
        
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
    
    // ... rest of DatabaseManager methods remain the same
}
