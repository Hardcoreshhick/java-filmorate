package ru.yandex.practicum.filmorate.storage.friendship;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FriendshipDbStorage implements FriendshipStorage {

    private final JdbcTemplate jdbc;
    private final UserMapper userMapper;
    private final UserStorage userStorage;
    private static final String SQL_INSERT_FRIEND =
            "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ? , ?)";
    private static final String SQL_DELETE_FRIEND =
            "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
    private static final String SQL_SELECT_FRIEND =
            "SELECT u.* FROM users u " +
                    "JOIN friends f ON f.friend_id = u.id " +
                    "WHERE f.user_id = ? AND f.status = ?";
    private static final String SQL_SELECT_COMMON_FRIENDS =
            "SELECT u.* FROM users u " +
                    "JOIN friends f1 ON f1.friend_id = u.id " +
                    "JOIN friends f2 ON f2.friend_id = u.id " +
                    "WHERE f1.user_id = ? AND f2.user_id = ? " +
                    "AND f1.status = ? AND f2.status = ? ORDER BY u.id";
    private static final String SQL_EXISTS_FRIEND =
            "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ? AND status = ?";
    private static final String SQL_UPDATE_FRIENDS =
            "UPDATE friends SET status = ? WHERE user_id = ? AND friend_id = ?";

    @Transactional
    @Override
    public void addFriend(long userId, long friendId) {
        if (!userStorage.exists(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (!userStorage.exists(friendId)) {
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }

        if (userId == friendId) {
            log.warn("Попытка добавить самого себя в друзья: {}", userId);
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        if (isFriend(userId, friendId)) {
            log.warn("Пользователь {} уже дружит с {}", userId, friendId);
            throw new DuplicatedDataException("Пользователь уже добавлен в друзья");
        }

        /*
        Вижу противоречие в тз

        1. Основной текст говорит: "дружба должна стать односторонней"
        должна добавляться только одна запись: userId -> friendId

        2. в подсказке напоминание написано: добавлять друг друга в друзья с подтверждением дружбы"
        нужны статусы Pending/Confirmed


        Решение:
        Добавляем только одну запись (односторонняя дружба, как в ТЗ).
        Статус CONFIRMED устанавливаем сразу (подтверждение не требуется).
        Методы confirmFriend() и getPendingFriends() оставлены для гибкости,
        но в текущей реализации не используются.
        */
        jdbc.update(SQL_INSERT_FRIEND, userId, friendId, FriendshipStatus.CONFIRMED.name());
        log.info("Пользователь {} добавил друга {}", userId, friendId);
    }

    @Transactional
    @Override
    public void removeFriend(long userId, long friendId) {
        if (userId == friendId) {
            log.warn("Попытка удалить самого себя из друзей: {}", userId);
            throw new ValidationException("Нельзя удалить самого себя из друзей");
        }

        log.debug("Пользователь {} удаляет друга {}", userId, friendId);

        // Удаляем запись о дружбе
        // Так как дружба односторонняя, удаляется только одна запись.
        int rowsDeleted = jdbc.update(SQL_DELETE_FRIEND, userId, friendId);

        if (rowsDeleted == 0) {
            log.warn("Дружба между {} и {} не найдена", userId, friendId);
            throw new NotFoundException("Дружба между пользователями " + userId + " и " + friendId + " не найдена");
        }
        log.info("Пользователь {} удалил друга {}", userId, friendId);
    }

    @Override
    public Collection<User> getFriends(long userId) {
        log.debug("Запрос друзей пользователя {}", userId);
        Collection<User> friends = jdbc.query(SQL_SELECT_FRIEND, userMapper, userId, FriendshipStatus.CONFIRMED.name());
        log.debug("Найдено {} друзей у пользователя {}", friends.size(), userId);
        return friends;
    }

    @Override
    public Collection<User> getCommonFriends(long userId, long friendId) {
        if (userId == friendId) {
            log.warn("Попытка получить общих друзей с самим собой: {}", userId);
            return getFriends(userId);  // или выбросить исключение
        }

        log.debug("Запрос общих друзей у {} и {}", userId, friendId);
        Collection<User> common = jdbc.query(SQL_SELECT_COMMON_FRIENDS, userMapper, userId, friendId,
                FriendshipStatus.CONFIRMED.name(),
                FriendshipStatus.CONFIRMED.name());
        log.debug("Найдено {} общих друзей", common.size());
        return common;
    }

    @Override
    public boolean isFriend(long userId, long friendId) {
        Integer count = jdbc.queryForObject(
                SQL_EXISTS_FRIEND,
                Integer.class,
                userId,
                friendId,
                FriendshipStatus.CONFIRMED.name()
        );
        return count > 0;
    }

    @Transactional
    @Override
    public void confirmFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new ValidationException("Нельзя подтвердить дружбу с самим собой");
        }
        log.debug("Пользователь {} подтверждает заявку от {}", userId, friendId);
        int rows = jdbc.update(SQL_UPDATE_FRIENDS, FriendshipStatus.CONFIRMED.name(), userId, friendId);
        if (rows == 0) {
            log.warn("Заявка в друзья между {} и {} не найдена", userId, friendId);
            throw new NotFoundException("Заявка в друзья не найдена");
        }
        log.info("Пользователь {} подтвердил дружбу с {}", userId, friendId);
    }

    @Override
    public Collection<User> getPendingFriends(long userId) {
        log.debug("Запрос заявок в друзья для пользователя {}", userId);
        return jdbc.query(SQL_SELECT_FRIEND, userMapper, userId, FriendshipStatus.PENDING.name());
    }

}
