package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@Slf4j
@Validated
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @GetMapping
    public Collection<User> findAll() {
        log.debug("Запрос на получение всех пользователей");
        Collection<User> result = users.values();
        log.info("Возвращено {} пользователей", result.size());
        return result;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        log.info("Запрос на добавление пользователя: {}", user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Пользователь добавлен: id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    @PutMapping("/{id}")
    public User update(@PathVariable @Positive Long id, @Valid @RequestBody User user) {
        log.debug("Запрос на обновление пользователя с id={}: {}", id, user);

        User oldUser = Optional.ofNullable(users.get(id))
                .orElseThrow(() -> {
                    log.warn("Попытка обновить несуществующего пользователя с id={}", id);
                    return new NotFoundException("Пользователь с id=" + id + " не найден");
                });

        oldUser.setEmail(user.getEmail());
        oldUser.setLogin(user.getLogin());
        oldUser.setName(user.getName());
        oldUser.setBirthday(user.getBirthday());

        log.info("Пользователь обновлён: id={}, login={}", id, oldUser.getLogin());
        return oldUser;
    }

    /**
     * Обновляет пользователя по id из тела запроса.
     * Костыль для совместимости с Postman-коллекцией, где PUT /users без id в URL.
     */
    @PutMapping
    public User updateWithoutId(@Valid @RequestBody User user) {
        log.debug("Запрос на обновление пользователя без id в пути: {}", user);

        if (user.getId() == null) {
            throw new NotFoundException("Id должен быть указан");
        }

        User oldUser = Optional.ofNullable(users.get(user.getId()))
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + user.getId() + " не найден"));

        oldUser.setEmail(user.getEmail());
        oldUser.setLogin(user.getLogin());
        oldUser.setName(user.getName());
        oldUser.setBirthday(user.getBirthday());

        log.info("Пользователь обновлён: id={}, login={}", user.getId(), oldUser.getLogin());
        return oldUser;
    }
}
