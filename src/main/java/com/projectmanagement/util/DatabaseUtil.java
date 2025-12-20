package com.projectmanagement.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    private static HikariDataSource dataSource;

    static {
        try {
            initializeDataSource();
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private static void initializeDataSource() throws IOException {
        Properties props = new Properties();
        
        // Essayer plusieurs chemins pour trouver db.properties
        InputStream is = null;
        
        // Essai 1: Via classloader (pour fichiers dans resources/)
        is = DatabaseUtil.class.getClassLoader().getResourceAsStream("db.properties");
        
        // Essai 2: Via File system
        if (is == null) {
            try {
                is = new java.io.FileInputStream("src/main/resources/db.properties");
            } catch (Exception e) {
                // Ignore
            }
        }
        
        // Essai 3: Fichier dans le dossier courant
        if (is == null) {
            try {
                is = new java.io.FileInputStream("db.properties");
            } catch (Exception e) {
                // Ignore
            }
        }
        
        if (is == null) {
            // Fallback: utiliser des valeurs par défaut
            logger.warn("db.properties not found, using default values");
            props.setProperty("db.url", "jdbc:mysql://localhost:3306/project_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
            props.setProperty("db.username", "root");
            props.setProperty("db.password", "");
            props.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        } else {
            props.load(is);
            is.close();
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
        config.setDriverClassName(props.getProperty("db.driver"));
        
        // Connection pool settings
        config.setMaximumPoolSize(Integer.parseInt(
                props.getProperty("db.pool.maxPoolSize", "20")));
        config.setMinimumIdle(Integer.parseInt(
                props.getProperty("db.pool.minIdle", "5")));
        config.setConnectionTimeout(Long.parseLong(
                props.getProperty("db.pool.connectionTimeout", "30000")));
        
        // Connection test and validation
        config.setConnectionTestQuery("SELECT 1");
        config.setAutoCommit(true);
        
        dataSource = new HikariDataSource(config);
        logger.info("Database connection pool initialized successfully");
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("DataSource is not initialized or is closed");
        }
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
    }

    // Test connection
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }
}
