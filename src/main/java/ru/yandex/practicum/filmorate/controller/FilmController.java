package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@Slf4j
@Validated
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.debug("Запрос на получение всех фильмов");
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable @Positive Long id) {
        log.debug("Запрос на получение фильма с id={}", id);
        return filmService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@Valid @RequestBody Film film) {
        log.info("Запрос на добавления фильма: {}", film);
        return filmService.create(film);
    }

    @PutMapping("/{id}")
    public Film update(@PathVariable @Positive Long id, @Valid @RequestBody Film film) {
        log.debug("Запрос на обновление фильма с id={}: {}", id, film);
        film.setId(id);
        return filmService.update(film);
    }

    /**
     * Обновляет фильм по id из тела запроса.
     * <p>
     * Метод для обратной совместимости с тестами.
     * Правильный REST-подход — PUT /films/{id}.
     */
    @PutMapping
    public Film updateWithoutId(@Valid @RequestBody Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        log.debug("Запрос на обновление фильма без id в пути: {}", film);
        return filmService.update(film);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        log.info("Запрос на удаление фильма с id={}", id);
        filmService.delete(id);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addLike(@PathVariable @Positive Long id, @PathVariable @Positive Long userId) {
        log.debug("Пользователь {} ставит лайк фильму {}", userId, id);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLike(@PathVariable @Positive Long id, @PathVariable @Positive Long userId) {
        log.debug("Пользователь {} удаляет лайк фильму {}", userId, id);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopular(@RequestParam(required = false)  Integer count) {
        log.debug("Запрос на получение {} популярных фильмов", count);
        return filmService.getPopular(count);
    }
}
