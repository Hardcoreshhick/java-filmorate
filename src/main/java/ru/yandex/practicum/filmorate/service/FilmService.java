package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final Map<Long, Set<Long>> likes = new HashMap<>();
    private List<Film> cachedPopularFilms;
    private boolean cacheDirty = true;

    @Value("${filmorate.popular.default-count:10}")
    private int defaultPopularCount;

    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new FilmNotFoundException("Фильм с id=" + id + " не найден"));
    }

    public Film create(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
        boolean exists = filmStorage.findAll().stream()
                .anyMatch(f -> f.getName().equalsIgnoreCase(film.getName()));
        if (exists) {
            log.warn("Попытка создать фильм с уже существующим названием: {}", film.getName());
            throw new DuplicatedDataException("Фильм с названием '" + film.getName() + "' уже существует");
        }
        log.info("Создание фильма: {}", film.getName());
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        log.info("Обновление фильма: {}", film);

        if (film.getId() == null) {
            log.warn("Попытка обновить фильм без id");
            throw new ValidationException("Id фильма должен быть указан");
        }

        Film existing = findById(film.getId());  // ← если нет — FilmNotFoundException

        if (film.getName() != null && !film.getName().isBlank()) {
            existing.setName(film.getName());
        }
        if (film.getDescription() != null && !film.getDescription().isBlank()) {
            existing.setDescription(film.getDescription());
        }
        if (film.getReleaseDate() != null) {
            existing.setReleaseDate(film.getReleaseDate());
        }
        if (film.getDuration() != null && film.getDuration() > 0) {
            existing.setDuration(film.getDuration());
        }

        log.info("Обновление фильма: id={}", film.getId());
        return filmStorage.update(existing);
    }

    public void delete(Long id) {
        log.info("Удаление фильма: id={}", id);
        filmStorage.delete(id);
    }

    public void addLike(long filmId, long userId) {
        userService.findById(userId);
        Film film = findById(filmId);
        likes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        cacheDirty = true;
        log.debug("Пользователь {} поставил лайк фильму {} ({})", userId, filmId, film.getName());
    }

    public void removeLike(long filmId, long userId) {
        userService.findById(userId);
        findById(filmId);

        if (!likes.containsKey(filmId)) {
            throw new DataNotFoundException("Лайк фильму " + filmId + " не найден");
        }
        Set<Long> filmLikes = likes.get(filmId);
        if (!filmLikes.contains(userId)) {
            throw new DataNotFoundException("Пользователь " + userId + " не ставил лайк фильму " + filmId);
        }
        filmLikes.remove(userId);
        cacheDirty = true;
        log.debug("Пользователь {} удалил лайк фильму {}", userId, filmId);
    }

    public int getLikesCount(long filmId) {
        return likes.getOrDefault(filmId, Collections.emptySet()).size();
    }

    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            log.debug("Передан некорректный count={}, используем значение по умолчанию: {}", count,
                    defaultPopularCount);
            count = defaultPopularCount;
        }

        if (!cacheDirty
                && count == defaultPopularCount
                && cachedPopularFilms != null
                && !cachedPopularFilms.isEmpty()) {
            log.debug("Возвращаем кешированный список популярных фильмов");
            return cachedPopularFilms;
        }

        List<Film> result = filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(
                        getLikesCount(f2.getId()),
                        getLikesCount(f1.getId())
                ))
                .limit(count)
                .collect(Collectors.toList());

        if (count == defaultPopularCount) {
            cachedPopularFilms = result;
            cacheDirty = false;
            log.debug("Кеш популярных фильмов обновлён");
        }

        return result;
    }
}
