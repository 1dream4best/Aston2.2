
package com.example.dao;

import com.example.entity.User;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    User save(Session session, User user);

    Optional<User> findById(Session session, Long id);

    List<User> findAll(Session session);

    boolean update(
            Session session,
            Long id,
            String name,
            String email,
            int age
    );

    boolean delete(Session session, Long id);
}
