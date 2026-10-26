package com.example.dao;

import com.example.config.HibernateUtil;
import com.example.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class UserDaoImplTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    private static SessionFactory sessionFactory;
    private UserDaoImpl userDao;

    @BeforeAll
    static void setUpDatabase() {
        System.setProperty(
                "hibernate.connection.url",
                POSTGRES.getJdbcUrl());

        System.setProperty(
                "hibernate.connection.username",
                POSTGRES.getUsername());

        System.setProperty(
                "DB_PASSWORD",
                POSTGRES.getPassword());

        sessionFactory = HibernateUtil.getSessionFactory();
    }

    @AfterAll
    static void closeSessionFactory() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }

        System.clearProperty("hibernate.connection.url");
        System.clearProperty("hibernate.connection.username");
        System.clearProperty("DB_PASSWORD");
    }

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl();

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            session.createMutationQuery("DELETE FROM User")
                    .executeUpdate();

            transaction.commit();
        }
    }

    private User createUser(String name, String email, int age) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setAge(age);
        return user;
    }

    @Test
    void save_shouldPersistUser() {
        User user = createUser("Иван", "ivan@test.com", 25);

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            userDao.save(session, user);

            transaction.commit();
            assertNotNull(user.getId());
        }

        try (Session session = sessionFactory.openSession()) {
            User found = session.find(User.class, user.getId());

            assertNotNull(found);
            assertEquals("Иван", found.getName());
            assertEquals("ivan@test.com", found.getEmail());
            assertEquals(25, found.getAge());
        }
    }

    @Test
    void findById_shouldReturnUserWhenExists() {
        User user = createUser("Анна", "anna@test.com", 30);
        saveUser(user);

        try (Session session = sessionFactory.openSession()) {
            Optional<User> result =
                    userDao.findById(session, user.getId());

            assertTrue(result.isPresent());
            assertEquals("Анна", result.get().getName());
        }
    }

    @Test
    void findById_shouldReturnEmptyWhenUserDoesNotExist() {
        try (Session session = sessionFactory.openSession()) {
            Optional<User> result = userDao.findById(session, -1L);

            assertTrue(result.isEmpty());
        }
    }

    @Test
    void findAll_shouldReturnUsersOrderedById() {
        saveUser(createUser("Первый", "first@test.com", 20));
        saveUser(createUser("Второй", "second@test.com", 25));

        try (Session session = sessionFactory.openSession()) {
            List<User> users = userDao.findAll(session);

            assertEquals(2, users.size());
            assertTrue(users.get(0).getId() < users.get(1).getId());
            assertEquals("Первый", users.get(0).getName());
            assertEquals("Второй", users.get(1).getName());
        }
    }

    @Test
    void update_shouldModifyExistingUser() {
        User user = createUser("Иван", "ivan@test.com", 25);
        saveUser(user);

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            boolean updated = userDao.update(
                    session,
                    user.getId(),
                    "Пётр",
                    "petr@test.com",
                    35);

            transaction.commit();
            assertTrue(updated);
        }

        try (Session session = sessionFactory.openSession()) {
            User updatedUser = session.find(User.class, user.getId());

            assertNotNull(updatedUser);
            assertEquals("Пётр", updatedUser.getName());
            assertEquals("petr@test.com", updatedUser.getEmail());
            assertEquals(35, updatedUser.getAge());
        }
    }

    @Test
    void update_shouldReturnFalseWhenUserDoesNotExist() {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            boolean updated = userDao.update(
                    session, -1L, "Пётр", "petr@test.com", 35
            );

            transaction.commit();
            assertFalse(updated);
        }
    }

    @Test
    void delete_shouldRemoveExistingUser() {
        User user = createUser("Иван", "ivan@test.com", 25);
        saveUser(user);

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            boolean deleted = userDao.delete(session, user.getId());

            transaction.commit();
            assertTrue(deleted);
        }

        try (Session session = sessionFactory.openSession()) {
            assertNull(session.find(User.class, user.getId()));
        }
    }

    @Test
    void delete_shouldReturnFalseWhenUserDoesNotExist() {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            boolean deleted = userDao.delete(session, -1L);

            transaction.commit();
            assertFalse(deleted);
        }
    }

    private void saveUser(User user) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            userDao.save(session, user);

            transaction.commit();
        }
    }
}
