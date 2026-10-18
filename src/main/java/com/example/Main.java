
package com.example;

import com.example.config.HibernateUtil;
import com.example.console.ConsoleApp;
import com.example.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log =
            LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            HibernateUtil.getSessionFactory();

            UserService userService = new UserService();
            ConsoleApp app = new ConsoleApp(userService);

            app.start();

        } catch (Exception e) {
            log.error("Не удалось запустить приложение", e);
            System.err.println(
                    "Ошибка запуска. Проверьте PostgreSQL и hibernate.cfg.xml.");
        } finally {
            try {
                HibernateUtil.shutdown();
            } catch (Throwable e) {
                log.error("Ошибка при закрытии Hibernate", e);
            }
        }
    }
}
