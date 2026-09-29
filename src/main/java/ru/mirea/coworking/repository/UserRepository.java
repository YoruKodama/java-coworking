package ru.mirea.coworking.repository;

import ru.mirea.coworking.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    List<User> findAll();

    Optional<User> findById(int id);

    boolean existsById(int id);

    boolean existsByEmail(String email);

    void update(User user);

    void deleteById(int id);
}
