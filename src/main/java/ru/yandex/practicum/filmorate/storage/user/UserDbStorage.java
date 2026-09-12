package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;
    private final UserMapper userMapper;
    private static final String SQL_INSERT_USER =
            "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String SQL_UPDATE_USER =
            "UPDATE users SET email=?, login=?, name=?, birthday=? WHERE id=?";
    private static final String SQL_SELECT_USER_BY_ID =
            "SELECT * FROM users WHERE id=?";
    private static final String SQL_DELETE_USER =
            "DELETE FROM users WHERE id=?";
    private static final String SQL_SELECT_ALL_USERS =
            "SELECT * FROM users ORDER BY id";
    private static final String SQL_EXISTS_USER =
            "SELECT COUNT(*) FROM users WHERE id = ?";

    @Transactional
    @Override
    public User create(User user) {
        log.debug("Создание пользователя {}", user.getEmail());
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        log.info("Создан пользователь с id: {}", user.getId());
        return user;
    }

    @Transactional
    @Override
    public User update(User user) {
        log.debug("Обновление пользователя с id: {}", user.getId());
        int rowsUpdated = jdbc.update(SQL_UPDATE_USER,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );

        if (rowsUpdated == 0) {
            log.warn("Пользователь с id: {} не найден", user.getId());
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }

        log.info("Обновлен пользователь с id {}", user.getId());
        return user;
    }

    @Override
    public Optional<User> findById(long id) {
        log.debug("Поиск пользователя по id: {}", id);
        List<User> users = jdbc.query(SQL_SELECT_USER_BY_ID, userMapper, id);
        return users.stream().findFirst();
    }

    @Override
    public Collection<User> findAll() {
        log.debug("Запрос всех пользователей");
        return jdbc.query(SQL_SELECT_ALL_USERS, userMapper);
    }

    @Transactional
    @Override
    public void delete(long id) {
        log.debug("Удаление пользователя с id: {}", id);
        int rowsDeleted = jdbc.update(SQL_DELETE_USER, id);

        if (rowsDeleted == 0) {
            log.warn("Пользователь с id {} не найден для удаления", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }

        log.info("Удален пользователь с id {}", id);
    }

    @Override
    public boolean exists(long id) {
        log.debug("Проверка существования пользователя с id: {}", id);
        Integer count = jdbc.queryForObject(SQL_EXISTS_USER, Integer.class, id);
        return count > 0;
    }

}
