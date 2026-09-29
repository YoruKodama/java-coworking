package ru.mirea.coworking;

import ru.mirea.coworking.console.ConsoleUI;
import ru.mirea.coworking.repository.UserRepository;
import ru.mirea.coworking.repository.UserRepositoryJdbc;
import ru.mirea.coworking.repository.WorkspaceRepository;
import ru.mirea.coworking.repository.WorkspaceRepositoryJdbc;
import ru.mirea.coworking.service.UserService;
import ru.mirea.coworking.service.WorkspaceService;

public class Main {
    public static void main(String[] args) {
        UserRepository userRepository = new UserRepositoryJdbc();
        WorkspaceRepository workspaceRepository = new WorkspaceRepositoryJdbc();

        UserService userService = new UserService(userRepository);
        WorkspaceService workspaceService = new WorkspaceService(workspaceRepository);

        new ConsoleUI(userService, workspaceService).run();
    }
}