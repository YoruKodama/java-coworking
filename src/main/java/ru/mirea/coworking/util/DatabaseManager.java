package ru.mirea.coworking.util;

import ru.mirea.coworking.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseManager {

    private static final String CONFIG_FILE = "db.properties";

    private static final String url;
    private static final String user;
    private static final String password;

    static {
        Properties properties = new Properties();
        try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new DatabaseException("Не найден файл конфигурации " + CONFIG_FILE);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new DatabaseException("Не удалось загрузить конфигурацию базы данных", e);
        }

        url = properties.getProperty("db.url");
        user = properties.getProperty("db.user");
        password = properties.getProperty("db.password");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("Драйвер PostgreSQL не найден в classpath", e);
        }
    }

    private DatabaseManager() {
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось подключиться к базе данных: " + e.getMessage(), e);
        }
    }
}
