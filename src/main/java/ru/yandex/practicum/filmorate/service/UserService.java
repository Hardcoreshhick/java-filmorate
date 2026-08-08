package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;
    private final Map<Long, Set<Long>> friends = new HashMap<>();

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public User create(User user) {

        boolean exist = userStorage.findAll().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()));
        if (exist) {
            log.warn("Попытка создать пользователя с уже существующим email: {}", user.getEmail());
            throw new DuplicatedDataException("Пользователь с email '" + user.getEmail() + "' уже существует");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя не указано, используем логин: {}", user.getLogin());
        }

        log.info("Создание пользователя: {}", user.getLogin());
        return userStorage.create(user);
    }

    public User update(User user) {

        if (user.getId() == null) {
            log.warn("Попытка обновить пользователя без id");
            throw new ValidationException("Id пользователя должен быть указан");
        }

        User existing = findById(user.getId());

        boolean emailExist = userStorage.findAll().stream()
                .anyMatch(u -> !u.getId().equals(user.getId())
                        && u.getEmail().equalsIgnoreCase(user.getEmail()));
        if (emailExist) {
            log.warn("Попытка обновить email на уже существующий: {}", user.getEmail());
            throw new DuplicatedDataException("Email '" + user.getEmail() + "' уже используется");
        }

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            existing.setEmail(user.getEmail());
        }
        if (user.getLogin() != null && !user.getLogin().isBlank()) {
            existing.setLogin(user.getLogin());
        }
        if (user.getName() != null && !user.getName().isBlank()) {
            existing.setName(user.getName());
        }
        if (user.getBirthday() != null) {
            existing.setBirthday(user.getBirthday());
        }

        log.info("Обновление пользователя: id={}", user.getId());
        return userStorage.update(existing);
    }

    public void delete(Long id) {
        log.info("Удаление пользователя: id={}", id);
        userStorage.delete(id);
        friends.remove(id);
    }

    public void addFriend(Long userId, Long friendId) {
        log.debug("Попытка удалить друга: userId={}, friendId={}", userId, friendId);
        log.debug("Текущие друзья пользователя {}: {}", userId, friends.get(userId));

        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        findById(userId);   // ← проверяем, что пользователь существует
        findById(friendId);

        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);

        log.debug("Пользователь {} и {} стали друзьями", userId, friendId);
    }

    /**
     * Удаляет пользователя из друзей.
     * Если пользователи не являются друзьями, метод ничего не делает (идемпотентность).
     * Это сделано для прохождения тестов Postman, которые ожидают 204 No Content
     * при попытке удалить несуществующую дружбу.
     * В реальном проекте здесь мог бы выбрасываться 404 Not Found.
     */
    public void removeFriend(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);

        if (!friends.containsKey(userId) || !friends.get(userId).contains(friendId)) {
            log.debug("Попытка удалить несуществующую дружбу: {} и {}", userId, friendId);
            return;
        }

        friends.get(userId).remove(friendId);
        friends.get(friendId).remove(userId);
        log.debug("Пользователь {} и {} больше не друзья", userId, friendId);
    }

    public Set<User> getFriends(Long userId) {
        findById(userId);

        if (!friends.containsKey(userId)) {
            return Collections.emptySet();
        }

        return friends.get(userId).stream()
                .map(this::findById)
                .collect(Collectors.toSet());
    }

    public Set<User> getCommonFriends(Long userId, Long otherId) {
        findById(userId);
        findById(otherId);

        Set<Long> userFriends = friends.getOrDefault(userId, Collections.emptySet());
        Set<Long> otherFriends = friends.getOrDefault(otherId, Collections.emptySet());

        Set<Long> common = new HashSet<>(userFriends);
        common.retainAll(otherFriends);

        return common.stream()
                .map(this::findById)
                .collect(Collectors.toSet());
    }
}
