package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;


    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
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
        validateUserExists(id);
        userStorage.delete(id);
        log.info("Пользователь с id {} удален", id);
    }

    public void addFriend(Long userId, Long friendId) {
        log.debug("Попытка добавить друга: userId={}, friendId={}", userId, friendId);

        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        validateUserExists(userId);
        validateUserExists(friendId);

        friendshipStorage.addFriend(userId, friendId);

        log.debug("Пользователь {} и {} стали друзьями", userId, friendId);
    }


    public void removeFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя удалить самого себя из друзей");
        }

        validateUserExists(userId);
        validateUserExists(friendId);

        if (!friendshipStorage.isFriend(userId, friendId)) {
            log.debug("Попытка удалить несуществующую дружбу: {} и {}", userId, friendId);
            return;
        }

        friendshipStorage.removeFriend(userId, friendId);

        log.debug("Пользователь {} и {} больше не друзья", userId, friendId);
    }

    public Collection<User> getFriends(Long userId) {
        log.debug("Запрос друзей пользователя с id: {}", userId);
        validateUserExists(userId);

        Collection<User> friends = friendshipStorage.getFriends(userId);

        log.info("Найдено {} друзей у пользователя с id: {}", friends.size(), userId);
        return friends;
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        log.debug("Запрос общих друзей у {} и {}", userId, otherId);
        validateUserExists(userId);
        validateUserExists(otherId);

        Collection<User> commonFriends = friendshipStorage.getCommonFriends(userId, otherId);
        log.info("Найдено {} общих друзей", commonFriends.size());

        return commonFriends;
    }

    public void confirmFriend(Long userId, Long friendId) {
        log.debug("Подтверждение дружбы: {} -> {}", userId, friendId);
        validateUserExists(userId);
        validateUserExists(friendId);
        friendshipStorage.confirmFriend(userId, friendId);
        log.info("Пользователь {} подтвердил дружбу с {}", userId, friendId);
    }

    private void validateUserExists(Long userId) {
        if (!userStorage.exists(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }
}
