
package com.example.dao;

import com.example.entity.User;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    private static final Logger log =
            LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public User save(Session session, User user) {
        session.persist(user);
        log.debug("Создание пользователя: {}", user.getEmail());
        return user;
    }

    @Override
    public Optional<User> findById(Session session, Long id) {
        return Optional.ofNullable(session.find(User.class, id));
    }

    @Override
    public List<User> findAll(Session session) {
        return session.createQuery(
                "FROM User u ORDER BY u.id",
                User.class
        ).getResultList();
    }

    @Override
    public boolean update(
            Session session,
            Long id,
            String name,
            String email,
            int age) {
        Optional<User> result = findById(session, id);

        if (result.isEmpty()) {
            return false;
        }

        User user = result.get();
        user.setName(name);
        user.setEmail(email);
        user.setAge(age);

        log.debug("Обновление пользователя id={}", id);
        return true;
    }

    @Override
    public boolean delete(Session session, Long id) {
        Optional<User> result = findById(session, id);

        if (result.isEmpty()) {
            return false;
        }

        session.remove(result.get());
        log.debug("Удаление пользователя id={}", id);
        return true;
    }
}
