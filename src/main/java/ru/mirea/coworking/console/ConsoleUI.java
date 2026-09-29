package ru.mirea.coworking.console;

import ru.mirea.coworking.exception.EntityNotFoundException;
import ru.mirea.coworking.exception.ValidationException;
import ru.mirea.coworking.model.User;
import ru.mirea.coworking.model.UserRole;
import ru.mirea.coworking.model.Workspace;
import ru.mirea.coworking.model.WorkspaceType;
import ru.mirea.coworking.service.UserService;
import ru.mirea.coworking.service.WorkspaceService;
import ru.mirea.coworking.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {

    private final Scanner scanner = new Scanner(System.in);
    private final UserService userService;
    private final WorkspaceService workspaceService;

    public ConsoleUI(UserService userService, WorkspaceService workspaceService) {
        this.userService = userService;
        this.workspaceService = workspaceService;
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Выберите действие: ");
            try {
                switch (choice) {
                    case 1 -> userMenu();
                    case 2 -> workspaceMenu();
                    case 3 -> System.out.println("Раздел «Бронирования» в разработке.");
                    case 4 -> System.out.println("Раздел «Поиск» в разработке.");
                    case 5 -> System.out.println("Раздел «Фильтрация» в разработке.");
                    case 6 -> System.out.println("Раздел «Статистика» в разработке.");
                    case 7 -> System.out.println("Раздел «Экспорт данных» в разработке.");
                    case 8 -> printAllTables();
                    case 0 -> running = false;
                    default -> System.out.println("Неизвестный пункт меню.");
                }
            } catch (ValidationException | EntityNotFoundException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Непредвиденная ошибка: " + e.getMessage());
            }
        }
        System.out.println("Работа программы завершена.");
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("      СИСТЕМА БРОНИРОВАНИЯ КОВОРКИНГА");
        System.out.println("========================================");
        System.out.println("1. Пользователи");
        System.out.println("2. Рабочие места");
        System.out.println("3. Бронирования");
        System.out.println("4. Поиск");
        System.out.println("5. Фильтрация");
        System.out.println("6. Статистика");
        System.out.println("7. Экспорт данных");
        System.out.println("8. Вывести таблицы базы данных");
        System.out.println("0. Выход");
    }

    // пользователи

    private void userMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("---------- Пользователи ----------");
            System.out.println("1. Создать пользователя");
            System.out.println("2. Показать всех пользователей");
            System.out.println("3. Найти пользователя по ID");
            System.out.println("4. Изменить пользователя");
            System.out.println("5. Удалить пользователя");
            System.out.println("6. Поиск пользователей (имя/email)");
            System.out.println("7. Фильтр по роли");
            System.out.println("8. Сортировка");
            System.out.println("0. Назад");
            int choice = readInt("Выберите действие: ");
            switch (choice) {
                case 1 -> createUser();
                case 2 -> printUsers(userService.findAll());
                case 3 -> printUser(userService.findById(readInt("Введите ID: ")));
                case 4 -> updateUser();
                case 5 -> deleteUser();
                case 6 -> printUsers(userService.search(readNonEmptyLine("Введите строку поиска: ")));
                case 7 -> filterUsersByRole();
                case 8 -> sortUsers();
                case 0 -> back = true;
                default -> System.out.println("Неизвестный пункт меню.");
            }
        }
    }

    private void createUser() {
        String fullName = readNonEmptyLine("ФИО: ");
        String email = readNonEmptyLine("Email: ");
        String phone = readLine("Телефон: ");
        UserRole role = readUserRole();
        User user = userService.create(new User(fullName, email, phone, role));
        System.out.println("Создан пользователь: " + user);
    }

    private void updateUser() {
        int id = readInt("Введите ID пользователя: ");
        User existing = userService.findById(id);
        System.out.println("Текущие данные: " + existing);
        String fullName = readNonEmptyLine("Новое ФИО: ");
        String email = readNonEmptyLine("Новый email: ");
        String phone = readLine("Новый телефон: ");
        UserRole role = readUserRole();
        User updated = userService.update(id, new User(fullName, email, phone, role));
        System.out.println("Обновлено: " + updated);
    }

    private void deleteUser() {
        int id = readInt("Введите ID пользователя: ");
        userService.delete(id);
        System.out.println("Пользователь удалён.");
    }

    private void filterUsersByRole() {
        UserRole role = readUserRole();
        printUsers(userService.filterByRole(role));
    }

    private void sortUsers() {
        System.out.println("1. По ФИО  2. По дате регистрации");
        int choice = readInt("Выбор: ");
        List<User> all = userService.findAll();
        List<User> sorted = (choice == 2) ? userService.sortByRegistrationDate(all) : userService.sortByName(all);
        printUsers(sorted);
    }

    private UserRole readUserRole() {
        while (true) {
            String input = readNonEmptyLine("Роль (CLIENT/ADMIN): ").toUpperCase();
            try {
                return UserRole.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: роль должна быть CLIENT или ADMIN.");
            }
        }
    }

    private void printUsers(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("Список пуст.");
            return;
        }
        users.forEach(this::printUser);
    }

    private void printUser(User user) {
        System.out.printf("[%d] %s | %s | %s | %s | %s%n",
                user.getId(), user.getFullName(), user.getEmail(), user.getPhone(), user.getRole(), user.getCreatedAt());
    }

    // рабочие места

    private void workspaceMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("---------- Рабочие места ----------");
            System.out.println("1. Создать рабочее место");
            System.out.println("2. Показать все рабочие места");
            System.out.println("3. Найти рабочее место по ID");
            System.out.println("4. Изменить рабочее место");
            System.out.println("5. Удалить рабочее место");
            System.out.println("6. Поиск по названию");
            System.out.println("7. Фильтр (тип/активность)");
            System.out.println("8. Сортировка");
            System.out.println("0. Назад");
            int choice = readInt("Выберите действие: ");
            switch (choice) {
                case 1 -> createWorkspace();
                case 2 -> printWorkspaces(workspaceService.findAll());
                case 3 -> printWorkspace(workspaceService.findById(readInt("Введите ID: ")));
                case 4 -> updateWorkspace();
                case 5 -> deleteWorkspace();
                case 6 -> printWorkspaces(workspaceService.search(readNonEmptyLine("Введите строку поиска: ")));
                case 7 -> filterWorkspaces();
                case 8 -> sortWorkspaces();
                case 0 -> back = true;
                default -> System.out.println("Неизвестный пункт меню.");
            }
        }
    }

    private void createWorkspace() {
        String name = readNonEmptyLine("Название: ");
        WorkspaceType type = readWorkspaceType();
        int capacity = readInt("Вместимость: ");
        BigDecimal price = readBigDecimal("Цена за час: ");
        boolean active = readYesNo("Активно? (y/n): ");
        Workspace workspace = workspaceService.create(new Workspace(name, type, capacity, price, active));
        System.out.println("Создано рабочее место: " + workspace);
    }

    private void updateWorkspace() {
        int id = readInt("Введите ID рабочего места: ");
        Workspace existing = workspaceService.findById(id);
        System.out.println("Текущие данные: " + existing);
        String name = readNonEmptyLine("Новое название: ");
        WorkspaceType type = readWorkspaceType();
        int capacity = readInt("Новая вместимость: ");
        BigDecimal price = readBigDecimal("Новая цена за час: ");
        boolean active = readYesNo("Активно? (y/n): ");
        Workspace updated = workspaceService.update(id, new Workspace(name, type, capacity, price, active));
        System.out.println("Обновлено: " + updated);
    }

    private void deleteWorkspace() {
        int id = readInt("Введите ID рабочего места: ");
        workspaceService.delete(id);
        System.out.println("Рабочее место удалено.");
    }

    private void filterWorkspaces() {
        System.out.println("1. По типу  2. По активности");
        int choice = readInt("Выбор: ");
        if (choice == 2) {
            boolean active = readYesNo("Показать активные? (y/n): ");
            printWorkspaces(workspaceService.filterByActive(active));
        } else {
            printWorkspaces(workspaceService.filterByType(readWorkspaceType()));
        }
    }

    private void sortWorkspaces() {
        System.out.println("1. По цене  2. По вместимости");
        int choice = readInt("Выбор: ");
        List<Workspace> all = workspaceService.findAll();
        List<Workspace> sorted = (choice == 2) ? workspaceService.sortByCapacity(all) : workspaceService.sortByPrice(all);
        printWorkspaces(sorted);
    }

    private WorkspaceType readWorkspaceType() {
        while (true) {
            System.out.println("Типы: HOT_DESK, MEETING_ROOM, PRIVATE_OFFICE, CONFERENCE_HALL");
            String input = readNonEmptyLine("Тип рабочего места: ").toUpperCase();
            try {
                return WorkspaceType.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: неизвестный тип рабочего места.");
            }
        }
    }

    private void printWorkspaces(List<Workspace> workspaces) {
        if (workspaces.isEmpty()) {
            System.out.println("Список пуст.");
            return;
        }
        workspaces.forEach(this::printWorkspace);
    }

    private void printWorkspace(Workspace workspace) {
        System.out.printf("[%d] %s | %s | вместимость: %d | %.2f/час | %s%n",
                workspace.getId(), workspace.getName(), workspace.getType(),
                workspace.getCapacity(), workspace.getPricePerHour(),
                workspace.isActive() ? "активно" : "неактивно");
    }

    // таблицы бд

    private void printAllTables() {
        List<String> tableNames = new ArrayList<>();
        String tablesSql = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(tablesSql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                tableNames.add(resultSet.getString("table_name"));
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении списка таблиц: " + e.getMessage());
            return;
        }

        for (String tableName : tableNames) {
            System.out.println();
            System.out.println("=== Таблица: " + tableName + " ===");
            printTableContents(tableName);
        }
    }

    private void printTableContents(String tableName) {
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName)) {

            ResultSetMetaData meta = resultSet.getMetaData();
            int columnCount = meta.getColumnCount();

            List<String> columnNames = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(meta.getColumnName(i));
            }
            System.out.println(String.join(" | ", columnNames));

            while (resultSet.next()) {
                List<String> values = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    values.add(resultSet.getString(i));
                }
                System.out.println(String.join(" | ", values));
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при чтении таблицы " + tableName + ": " + e.getMessage());
        }
    }

    // ввод данных

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: значение должно быть целым числом.");
            }
        }
    }

    private BigDecimal readBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().replace(",", ".");
            try {
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: значение должно быть числом.");
            }
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private String readNonEmptyLine(String prompt) {
        while (true) {
            String input = readLine(prompt);
            if (!input.isBlank()) {
                return input;
            }
            System.out.println("Ошибка: поле не может быть пустым.");
        }
    }

    private boolean readYesNo(String prompt) {
        while (true) {
            String input = readLine(prompt).toLowerCase();
            if (input.equals("y") || input.equals("yes") || input.equals("да")) return true;
            if (input.equals("n") || input.equals("no") || input.equals("нет")) return false;
            System.out.println("Ошибка: введите y или n.");
        }
    }
}
