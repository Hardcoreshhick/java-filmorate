package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.Set;

@RestController
@RequestMapping("/users")
@Slf4j
@Validated
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<User> findAll() {
        log.debug("Запрос на получение всех пользователей");
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable @Positive Long id) {
        log.debug("Запрос на получение пользователя с id={}", id);
        return userService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        log.info("Запрос на создание пользователя: {}", user.getLogin());
        return userService.create(user);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public User update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody User user
    ) {
        log.info("Запрос на обновление пользователя с id={}", id);
        user.setId(id);
        User updated = userService.update(user);
        log.info("Пользователь с id={} обновлён", id);  // ← добавить
        return updated;
    }

    /**
     * Обновляет пользователя по id из тела запроса.
     * Этот метод существует для обратной совместимости с тестами Postman,
     * которые отправляют PUT /users без id в пути.
     * В REST-стандарте правильным является PUT /users/{id}.
     */
    @PutMapping
    public User updateWithoutId(@Valid @RequestBody User user) {
        if (user.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        return userService.update(user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        log.info("Запрос на удаление пользователя с id={}", id);
        userService.delete(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFriend(
            @PathVariable @Positive Long id,
            @PathVariable @Positive Long friendId
    ) {
        log.debug("Пользователь {} добавляет в друзья {}", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFriend(
            @PathVariable @Positive Long id,
            @PathVariable @Positive Long friendId
    ) {
        log.debug("Пользователь {} удаляет из друзей {}", id, friendId);
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Set<User> getFriends(@PathVariable @Positive Long id) {
        log.debug("Запрос на получение друзей пользователя {}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Set<User> getCommonFriends(
            @PathVariable @Positive Long id,
            @PathVariable @Positive Long otherId
    ) {
        log.debug("Запрос на получение общих друзей пользователей {} и {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }

}
