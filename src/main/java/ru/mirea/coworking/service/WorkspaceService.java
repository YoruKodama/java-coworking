package ru.mirea.coworking.service;

import ru.mirea.coworking.exception.EntityNotFoundException;
import ru.mirea.coworking.exception.ValidationException;
import ru.mirea.coworking.model.Workspace;
import ru.mirea.coworking.model.WorkspaceType;
import ru.mirea.coworking.repository.WorkspaceRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public class WorkspaceService {

    // ограничения соответствуют колонкам в БД: VARCHAR(100) и NUMERIC(10, 2)
    private static final int MAX_NAME_LENGTH = 100;
    private static final BigDecimal MAX_PRICE = new BigDecimal("99999999.99");

    private final WorkspaceRepository workspaceRepository;

    public WorkspaceService(WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }

    public Workspace create(Workspace workspace) {
        validate(workspace);
        if (workspaceRepository.existsByName(workspace.getName())) {
            throw new ValidationException("Рабочее место с названием '" + workspace.getName() + "' уже существует");
        }
        return workspaceRepository.save(workspace);
    }

    public List<Workspace> findAll() {
        return workspaceRepository.findAll();
    }

    public Workspace findById(int id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Рабочее место с ID " + id + " не найдено"));
    }

    public Workspace update(int id, Workspace updated) {
        Workspace existing = findById(id);
        validate(updated);

        if (!existing.getName().equalsIgnoreCase(updated.getName())
                && workspaceRepository.existsByName(updated.getName())) {
            throw new ValidationException("Рабочее место с названием '" + updated.getName() + "' уже существует");
        }

        updated.setId(id);
        workspaceRepository.update(updated);
        return updated;
    }

    public void delete(int id) {
        findById(id);
        workspaceRepository.deleteById(id);
    }

    public boolean exists(int id) {
        return workspaceRepository.existsById(id);
    }

    public List<Workspace> search(String query) {
        String needle = query.toLowerCase();
        return workspaceRepository.findAll().stream()
                .filter(w -> w.getName().toLowerCase().contains(needle))
                .toList();
    }

    public List<Workspace> filterByType(WorkspaceType type) {
        return workspaceRepository.findAll().stream()
                .filter(w -> w.getType() == type)
                .toList();
    }

    public List<Workspace> filterByActive(boolean active) {
        return workspaceRepository.findAll().stream()
                .filter(w -> w.isActive() == active)
                .toList();
    }

    public List<Workspace> sortByPrice(List<Workspace> workspaces) {
        return workspaces.stream()
                .sorted(Comparator.comparing(Workspace::getPricePerHour))
                .toList();
    }

    public List<Workspace> sortByCapacity(List<Workspace> workspaces) {
        return workspaces.stream()
                .sorted(Comparator.comparingInt(Workspace::getCapacity))
                .toList();
    }

    private void validate(Workspace workspace) {
        if (workspace.getName() == null || workspace.getName().isBlank()) {
            throw new ValidationException("Название рабочего места не может быть пустым");
        }
        if (workspace.getName().length() > MAX_NAME_LENGTH) {
            throw new ValidationException("Название не может быть длиннее " + MAX_NAME_LENGTH + " символов");
        }
        if (workspace.getType() == null) {
            throw new ValidationException("Тип рабочего места обязателен");
        }
        if (workspace.getCapacity() <= 0) {
            throw new ValidationException("Вместимость должна быть больше нуля");
        }
        if (workspace.getPricePerHour() == null || workspace.getPricePerHour().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Цена за час должна быть больше нуля");
        }
        if (workspace.getPricePerHour().compareTo(MAX_PRICE) > 0) {
            throw new ValidationException("Цена за час не может быть больше " + MAX_PRICE);
        }
    }
}
