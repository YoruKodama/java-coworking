package ru.mirea.coworking.repository;

import ru.mirea.coworking.model.Workspace;

import java.util.List;
import java.util.Optional;

public interface WorkspaceRepository {

    Workspace save(Workspace workspace);

    List<Workspace> findAll();

    Optional<Workspace> findById(int id);

    boolean existsById(int id);

    boolean existsByName(String name);

    void update(Workspace workspace);

    void deleteById(int id);
}
