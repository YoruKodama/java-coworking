package ru.mirea.coworking.service;

import ru.mirea.coworking.exception.EntityNotFoundException;
import ru.mirea.coworking.exception.ValidationException;
import ru.mirea.coworking.model.User;
import ru.mirea.coworking.model.UserRole;
import ru.mirea.coworking.repository.UserRepository;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class UserService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9\\-() ]{7,30}$");
    // длины совпадают с размерами колонок в БД
    private static final int MAX_NAME_LENGTH = 150;
    private static final int MAX_EMAIL_LENGTH = 150;

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User create(User user) {
        validate(user);
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ValidationException("Пользователь с email '" + user.getEmail() + "' уже существует");
        }
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID " + id + " не найден"));
    }

    public User update(int id, User updated) {
        User existing = findById(id);
        validate(updated);

        if (!existing.getEmail().equalsIgnoreCase(updated.getEmail())
                && userRepository.existsByEmail(updated.getEmail())) {
            throw new ValidationException("Пользователь с email '" + updated.getEmail() + "' уже существует");
        }

        updated.setId(id);
        userRepository.update(updated);
        return updated;
    }

    public void delete(int id) {
        findById(id);
        userRepository.deleteById(id);
    }

    public boolean exists(int id) {
        return userRepository.existsById(id);
    }

    public List<User> search(String query) {
        String needle = query.toLowerCase();
        return userRepository.findAll().stream()
                .filter(u -> u.getFullName().toLowerCase().contains(needle)
                        || u.getEmail().toLowerCase().contains(needle))
                .toList();
    }

    public List<User> filterByRole(UserRole role) {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == role)
                .toList();
    }

    public List<User> sortByName(List<User> users) {
        return users.stream()
                .sorted(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<User> sortByRegistrationDate(List<User> users) {
        return users.stream()
                .sorted(Comparator.comparing(User::getCreatedAt))
                .toList();
    }

    private void validate(User user) {
        if (user.getFullName() == null || user.getFullName().isBlank()) {
            throw new ValidationException("ФИО пользователя не может быть пустым");
        }
        if (user.getFullName().length() > MAX_NAME_LENGTH) {
            throw new ValidationException("ФИО не может быть длиннее " + MAX_NAME_LENGTH + " символов");
        }
        if (user.getEmail() == null || !EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new ValidationException("Некорректный email: " + user.getEmail());
        }
        if (user.getEmail().length() > MAX_EMAIL_LENGTH) {
            throw new ValidationException("Email не может быть длиннее " + MAX_EMAIL_LENGTH + " символов");
        }
        // телефон необязателен, но если указан - должен соответствовать формату
        if (user.getPhone() != null && !user.getPhone().isBlank()
                && !PHONE_PATTERN.matcher(user.getPhone()).matches()) {
            throw new ValidationException("Некорректный телефон: " + user.getPhone()
                    + " (пример: +7-900-100-00-01)");
        }
        // email хранится в нижнем регистре, чтобы A@x.com и a@x.com считались одним адресом
        user.setEmail(user.getEmail().toLowerCase());
        if (user.getRole() == null) {
            throw new ValidationException("Роль пользователя обязательна");
        }
    }
}
