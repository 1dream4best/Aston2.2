package com.example.service;

import com.example.dao.UserDao;
import com.example.entity.User;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Transaction transaction;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userDao, sessionFactory);
    }

    @Test
    void createUser_shouldSaveUserAndCommitTransaction() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);

        User user = userService.createUser(
                "Ivan",
                "ivan@example.com",
                25);

        assertNotNull(user);
        assertEquals("Ivan", user.getName());
        assertEquals("ivan@example.com", user.getEmail());

        verify(userDao).save(session, user);
        verify(transaction).commit();
        verify(session).close();
    }

    @Test
    void createUser_shouldRejectBlankName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(
                        " ",
                        "ivan@example.com",
                        25));

        verifyNoInteractions(userDao, sessionFactory);
    }

    @Test
    void createUser_shouldRejectInvalidEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(
                        "Ivan",
                        "invalid-email",
                        25));

        verifyNoInteractions(userDao, sessionFactory);
    }

    @Test
    void createUser_shouldRejectNegativeAge() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(
                        "Ivan",
                        "ivan@example.com",
                        -1));

        verifyNoInteractions(userDao, sessionFactory);
    }

    @Test
    void getUserById_shouldReturnUserWhenFound() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);

        User expected = new User(
                "Ivan",
                "ivan@example.com",
                25);

        when(userDao.findById(session, 1L))
                .thenReturn(Optional.of(expected));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertSame(expected, result.get());

        verify(userDao).findById(session, 1L);
        verify(transaction).commit();
        verify(session).close();
    }

    @Test
    void getUserById_shouldRejectInvalidId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserById(0L));

        verifyNoInteractions(userDao, sessionFactory);
    }

    @Test
    void createUser_shouldTranslateDuplicateEmailException() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);
        when(transaction.isActive()).thenReturn(true);

        SQLException sqlException = new SQLException(
                "Duplicate email",
                "23505");

        ConstraintViolationException databaseException =
                new ConstraintViolationException(
                        "Duplicate email",
                        sqlException,
                        "users_email_key");

        doThrow(databaseException)
                .when(userDao)
                .save(eq(session), any(User.class));

        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> userService.createUser(
                        "Ivan",
                        "ivan@example.com",
                        25));

        assertEquals(
                "Пользователь с таким email уже существует",
                exception.getMessage());

        verify(transaction).rollback();
        verify(session).close();
    }
}
