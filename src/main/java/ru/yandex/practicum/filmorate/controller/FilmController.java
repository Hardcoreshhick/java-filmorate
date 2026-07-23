package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    private long nextId = 1;

    @GetMapping
    public Collection<Film> findAll() {

        log.debug("Запрос на получение всех фильмов");
        Collection<Film> result = films.values();
        log.info("Возвращено {} фильмов", result.size());
        return result;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@Valid @RequestBody Film film) {
        log.info("Запрос на добавления фильма: {}", film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм добавлен: {}, name={}", film.getId(), film.getName());
        return film;
    }

    @PutMapping("/{id}")
    public Film update(@PathVariable Long id, @Valid @RequestBody Film film) {
        log.debug("Запрос на обновление фильма с id={}: {}", id, film);

        Film oldFilm = Optional.ofNullable(films.get(id))
                .orElseThrow(() -> {
                    log.warn("Попытка обновить несуществующий фильм с id={}", id);
                    return new NotFoundException("Фильм с id =" + id + " не найден");
                });

        oldFilm.setName(film.getName());
        oldFilm.setDescription(film.getDescription());
        oldFilm.setReleaseDate(film.getReleaseDate());
        oldFilm.setDuration(film.getDuration());

        log.info("Фильм обновлён: id={}, name={}", id, oldFilm.getName());
        return oldFilm;
    }

    /**
     * Обновляет фильм по id.
     * REST-стандарт: PUT /films/{id}
     * В Postman-коллекции используется PUT /films (id в теле) — это не соответствует REST.
     */
    @PutMapping
    public Film updateWithoutId(@Valid @RequestBody Film film) {
        log.debug("Запрос на обновление фильма без id в пути: {}", film);

        if (film.getId() == null) {
            throw new NotFoundException("Id должен быть указан");
        }

        Film oldFilm = Optional.ofNullable(films.get(film.getId()))
                .orElseThrow(() -> new NotFoundException("Фильм с id =" + film.getId() + " не найден"));

        oldFilm.setName(film.getName());
        oldFilm.setDescription(film.getDescription());
        oldFilm.setReleaseDate(film.getReleaseDate());
        oldFilm.setDuration(film.getDuration());

        log.info("Фильм обновлён: id={}, name={}", film.getId(), oldFilm.getName());
        return oldFilm;
    }
}
