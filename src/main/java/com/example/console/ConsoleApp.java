
package com.example.console;

import com.example.entity.User;
import com.example.service.UserService;
import com.example.service.DuplicateEmailException;

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Scanner;

public class ConsoleApp {

    private static final Logger log =
            LoggerFactory.getLogger(ConsoleApp.class);

    private final UserService userService;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleApp(UserService userService) {
        this.userService = userService;
    }

    public void start() {
        boolean running = true;

        System.out.println("=== USER SERVICE ===");

        while (running) {
            printMenu();
            String choice = readLine("Выберите действие: ");

            try {
                switch (choice) {
                    case "1" -> createUser();
                    case "2" -> findUser();
                    case "3" -> showAllUsers();
                    case "4" -> updateUser();
                    case "5" -> deleteUser();
                    case "0" -> running = false;
                    default -> System.out.println("Неизвестная команда");
                }

            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());

            } catch (DuplicateEmailException e) {
                System.out.println("Ошибка: " + e.getMessage());

            } catch (HibernateException e) {
                System.out.println(
                        "Ошибка базы данных. Подробности записаны в лог.");
                log.error("Ошибка базы данных", e);

            } catch (RuntimeException e) {
                System.out.println(
                        "Операция не выполнена. Подробности записаны в лог.");
                log.error("Ошибка приложения", e);
            }
        }

        System.out.println("Приложение завершено.");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. Создать пользователя");
        System.out.println("2. Найти пользователя по ID");
        System.out.println("3. Показать всех пользователей");
        System.out.println("4. Обновить пользователя");
        System.out.println("5. Удалить пользователя");
        System.out.println("0. Выход");
    }

    private void createUser() {
        String name = readLine("Имя: ");
        String email = readLine("Email: ");
        int age = readInt("Возраст: ");

        User user = userService.createUser(name, email, age);

        System.out.println("Пользователь создан:");
        System.out.println(user);
    }

    private void findUser() {
        long id = readLong("Введите ID: ");
        System.out.println(userService.getUserById(id));
    }

    private void showAllUsers() {
        List<User> users = userService.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("Пользователи не найдены.");
            return;
        }

        users.forEach(System.out::println);
    }

    private void updateUser() {
        long id = readLong("ID пользователя: ");
        String name = readLine("Новое имя: ");
        String email = readLine("Новый email: ");
        int age = readInt("Новый возраст: ");

        boolean updated = userService.updateUser(id, name, email, age);

        System.out.println(updated
                ? "Пользователь обновлён."
                : "Пользователь не найден.");
    }

    private void deleteUser() {
        long id = readLong("ID пользователя: ");
        boolean deleted = userService.deleteUser(id);

        System.out.println(deleted
                ? "Пользователь удалён."
                : "Пользователь не найден.");
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt) {
        try {
            return Integer.parseInt(readLine(prompt));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Введите целое число");
        }
    }

    private long readLong(String prompt) {
        try {
            return Long.parseLong(readLine(prompt));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "ID должен быть целым числом");
        }
    }
}