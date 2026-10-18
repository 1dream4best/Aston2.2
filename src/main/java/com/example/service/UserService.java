
package com.example.service;

import com.example.config.HibernateUtil;
import com.example.dao.UserDao;
import com.example.dao.UserDaoImpl;
import com.example.entity.User;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.JDBCConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

public class UserService {

    private static final Logger log =
            LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao = new UserDaoImpl();

    private <T> T executeInTransaction(Function<Session, T> operation) {
        Transaction transaction = null;

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            transaction = session.beginTransaction();

            T result = operation.apply(session);

            transaction.commit();
            return result;

        } catch (RuntimeException e) {

            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (RuntimeException rollbackError) {
                    e.addSuppressed(rollbackError);
                    log.error("Ошибка отката транзакции", rollbackError);
                }
            }

            if (e instanceof ConstraintViolationException) {
                log.error("Нарушено ограничение БД", e);
            } else if (e instanceof JDBCConnectionException) {
                log.error("Нет соединения с PostgreSQL", e);
            } else {
                log.error("Ошибка операции с пользователем", e);
            }

            throw e;
        }
    }

    public User createUser(String name, String email, int age) {
        validateUser(name, email, age);

        return executeInTransaction(session ->
                userDao.save(session, new User(name, email, age)));
    }

    public User getUserById(Long id) {
        validateId(id);

        return executeInTransaction(session ->
                userDao.findById(session, id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Пользователь с ID " + id + " не найден" )));
    }

    public List<User> getAllUsers() {
        return executeInTransaction(userDao::findAll);
    }

    public boolean updateUser(
            Long id,
            String name,
            String email,
            int age) {
        validateId(id);
        validateUser(name, email, age);

        return executeInTransaction(session ->
                userDao.update(session, id, name, email, age));
    }

    public boolean deleteUser(Long id) {
        validateId(id);

        return executeInTransaction(session ->
                userDao.delete(session, id));
    }

    private void validateUser(String name, String email, int age) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя не должно быть пустым");
        }

        if (name.length() > 100) {
            throw new IllegalArgumentException("Максимальная длина имени — 100 символов");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email не должен быть пустым");
        }

        if (email.length() > 255 ||
                !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Некорректный email");
        }

        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Возраст должен быть от 0 до 150");
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID должен быть положительным числом");
        }
    }
}
