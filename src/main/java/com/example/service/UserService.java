package com.example.service;

import com.example.config.HibernateUtil;
import com.example.dao.UserDao;
import com.example.dao.UserDaoImpl;
import com.example.entity.User;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class UserService {

    private final UserDao userDao;
    private final SessionFactory sessionFactory;

    public UserService() {
        this(new UserDaoImpl(), HibernateUtil.getSessionFactory());
    }

    public UserService(UserDao userDao, SessionFactory sessionFactory) {
        this.userDao = userDao;
        this.sessionFactory = sessionFactory;
    }

    public User createUser(String name, String email, int age) {
        validateUser(name, email, age);

        User user = new User(name.trim(), email.trim(), age);

        return executeInTransaction(session -> {
            userDao.save(session, user);
            return user;
        });
    }

    public Optional<User> getUserById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID должен быть положительным числом");
        }

        return executeInTransaction(
                session -> userDao.findById(session, id));
    }

    public List<User> getAllUsers() {
        return executeInTransaction(userDao::findAll);
    }

    public boolean updateUser(Long id, String name, String email, int age) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID должен быть положительным числом");
        }

        validateUser(name, email, age);

        return executeInTransaction(session ->
                userDao.update(
                        session,
                        id,
                        name.trim(),
                        email.trim(),
                        age));
    }

    public boolean deleteUser(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID должен быть положительным числом");
        }

        return executeInTransaction(session -> userDao.delete(session, id));
    }

    private void validateUser(String name, String email, int age) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя не должно быть пустым");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email не должен быть пустым");
        }

        if (!email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Некорректный формат email");
        }

        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Возраст должен быть от 0 до 150");
        }
    }

    private <T> T executeInTransaction(Function<Session, T> action) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            try {
                T result = action.apply(session);
                transaction.commit();
                return result;
            } catch (RuntimeException e) {
                if (transaction.isActive()) {
                    try {
                        transaction.rollback();
                    } catch (RuntimeException rollbackError) {
                        e.addSuppressed(rollbackError);
                    }
                }

                throw translateException(e);
            }
        }
    }

    private RuntimeException translateException(RuntimeException error) {
        if (isDuplicateEmail(error)) {
            return new DuplicateEmailException(
                    "Пользователь с таким email уже существует");
        }

        return error;
    }

    private boolean isDuplicateEmail(Throwable error) {
        Throwable current = error;

        while (current != null) {
            if (current instanceof ConstraintViolationException constraintError) {
                if ("23505".equals(constraintError.getSQLState())) {
                    return true;
                }
            }

            current = current.getCause();
        }

        return false;
    }
}