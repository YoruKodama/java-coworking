package ru.mirea.coworking.repository;

import ru.mirea.coworking.exception.DatabaseException;
import ru.mirea.coworking.model.Workspace;
import ru.mirea.coworking.model.WorkspaceType;
import ru.mirea.coworking.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkspaceRepositoryJdbc implements WorkspaceRepository {

    @Override
    public Workspace save(Workspace workspace) {
        String sql = "INSERT INTO workspaces (name, type, capacity, price_per_hour, is_active) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, workspace.getName());
            statement.setString(2, workspace.getType().name());
            statement.setInt(3, workspace.getCapacity());
            statement.setBigDecimal(4, workspace.getPricePerHour());
            statement.setBoolean(5, workspace.isActive());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    workspace.setId(keys.getInt(1));
                }
            }
            return workspace;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при сохранении рабочего места: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Workspace> findAll() {
        String sql = "SELECT * FROM workspaces ORDER BY id";
        List<Workspace> workspaces = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                workspaces.add(mapRow(resultSet));
            }
            return workspaces;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при получении списка рабочих мест: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Workspace> findById(int id) {
        String sql = "SELECT * FROM workspaces WHERE id = ?";
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
            throw new DatabaseException("Ошибка при поиске рабочего места по ID: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(int id) {
        String sql = "SELECT 1 FROM workspaces WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при проверке существования рабочего места: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByName(String name) {
        String sql = "SELECT 1 FROM workspaces WHERE name = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при проверке названия рабочего места: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Workspace workspace) {
        String sql = "UPDATE workspaces SET name = ?, type = ?, capacity = ?, price_per_hour = ?, is_active = ? WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, workspace.getName());
            statement.setString(2, workspace.getType().name());
            statement.setInt(3, workspace.getCapacity());
            statement.setBigDecimal(4, workspace.getPricePerHour());
            statement.setBoolean(5, workspace.isActive());
            statement.setInt(6, workspace.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при обновлении рабочего места: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(int id) {
        String sql = "DELETE FROM workspaces WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при удалении рабочего места: " + e.getMessage(), e);
        }
    }

    private Workspace mapRow(ResultSet resultSet) throws SQLException {
        return new Workspace(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                WorkspaceType.valueOf(resultSet.getString("type")),
                resultSet.getInt("capacity"),
                resultSet.getBigDecimal("price_per_hour"),
                resultSet.getBoolean("is_active")
        );
    }
}
