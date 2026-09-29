package ru.mirea.coworking.repository;

import ru.mirea.coworking.exception.DatabaseException;
import ru.mirea.coworking.model.User;
import ru.mirea.coworking.model.UserRole;
import ru.mirea.coworking.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepositoryJdbc implements UserRepository {

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (full_name, email, phone, role) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setString(4, user.getRole().name());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при сохранении пользователя: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY id";
        List<User> users = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapRow(resultSet));
            }
            return users;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при получении списка пользователей: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при поиске пользователя по ID: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(int id) {
        String sql = "SELECT 1 FROM users WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при проверке существования пользователя: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при проверке email пользователя: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, role = ? WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setString(4, user.getRole().name());
            statement.setInt(5, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при обновлении пользователя: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при удалении пользователя: " + e.getMessage(), e);
        }
    }

    private User mapRow(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("id"),
                resultSet.getString("full_name"),
                resultSet.getString("email"),
                resultSet.getString("phone"),
                UserRole.valueOf(resultSet.getString("role")),
                resultSet.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
