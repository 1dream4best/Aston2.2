package com.example.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HibernateUtil {

    private static final Logger log =
            LoggerFactory.getLogger(HibernateUtil.class);

    private static final SessionFactory SESSION_FACTORY =
            buildSessionFactory();

    private HibernateUtil() {
    }

    private static SessionFactory buildSessionFactory() {
        String password = System.getenv("DB_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new ExceptionInInitializerError(
                    "Не задана переменная окружения DB_PASSWORD. " +
                            "Настрой её перед запуском приложения.");
        }

        try {
            Configuration configuration =
                    new Configuration().configure("hibernate.cfg.xml");

            configuration.setProperty(
                    "hibernate.connection.password",
                    password);

            SessionFactory factory =
                    configuration.buildSessionFactory();

            log.info("Hibernate успешно инициализирован");
            return factory;

        } catch (Exception e) {
            log.error("Не удалось инициализировать Hibernate", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    public static void shutdown() {
        if (!SESSION_FACTORY.isClosed()) {
            SESSION_FACTORY.close();
            log.info("Hibernate завершил работу");
        }
    }
}
