package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikeStorage likeStorage;
    private final MpaDbStorage mpaDbStorage;
    private final GenreStorage genreStorage;

    @Value("${filmorate.popular.default-count:10}")
    private int defaultPopularCount;


    public Collection<Film> findAll() {
        log.debug("Запрос всех фильмов");
        Collection<Film> films = filmStorage.findAll();
        films.forEach(film -> film.setGenres(genreStorage.getGenresForFilm(film.getId())));
        return films;
    }

    public Film findById(Long id) {
        log.debug("Поиск фильма по id {}", id);
        Film film = filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
        film.setGenres(genreStorage.getGenresForFilm(film.getId()));
        return film;
    }

    @CacheEvict(value = "popularFilms", allEntries = true)
    public Film create(Film film) {
        log.debug("Создание фильма: {}", film.getName());
        validateName(film);
        validateGenres(film);
        validateReleaseDate(film);
        validateDuration(film);
        validateMpa(film);

        Film created = filmStorage.create(film);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreStorage.addGenresToFilm(created.getId(), film.getGenres());
        }

        log.info("Создан фильм с id: {}", created.getId());
        return created;
    }

    @CacheEvict(value = "popularFilms", allEntries = true)
    public Film update(Film film) {
        log.debug("Обновление фильма с id: {}", film.getId());

        if (film.getId() == null) {
            throw new ValidationException("id фильма должен быть указан");
        }

        validateFilmExists(film.getId());
        Film existing = filmStorage.findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id " + film.getId() + " не найден"));


        if (film.getName() != null) {
            validateName(film);
            existing.setName(film.getName());
        }
        if (film.getDescription() != null && !film.getDescription().isBlank()) {
            existing.setDescription(film.getDescription());
        }
        if (film.getReleaseDate() != null) {
            validateReleaseDate(film);
            existing.setReleaseDate(film.getReleaseDate());
        }
        if (film.getDuration() != null) {
            validateDuration(film);
            existing.setDuration(film.getDuration());
        }
        if (film.getMpaRating() != null) {
            validateMpa(film);
            existing.setMpaRating(film.getMpaRating());
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            validateGenres(film);
            genreStorage.deleteGenresFromFilm(film.getId());
            genreStorage.addGenresToFilm(film.getId(), film.getGenres());
        } else {
            genreStorage.deleteGenresFromFilm(film.getId());
        }

        log.info("Обновлён фильм с id: {}", film.getId());
        return filmStorage.update(existing);
    }

    @CacheEvict(value = "popularFilms", allEntries = true)
    public void delete(Long id) {
        log.debug("Удаление фильма: id={}", id);
        validateFilmExists(id);
        filmStorage.delete(id);
        log.info("Фильм с id {} удален", id);
    }

    @CacheEvict(value = "popularFilms", allEntries = true)
    public void addLike(long filmId, long userId) {
        log.debug("Пользователь {} ставит лайк фильму {}", userId, filmId);

        validateFilmExists(filmId);
        validateUserExists(userId);

        likeStorage.addLike(filmId, userId);
        log.debug("Пользователь {} поставил лайк фильму {} ", userId, filmId);
    }

    @CacheEvict(value = "popularFilms", allEntries = true)
    public void removeLike(long filmId, long userId) {
        log.debug("Пользователь {} убирает лайк фильму {}", userId, filmId);
        validateFilmExists(filmId);
        validateUserExists(userId);
        likeStorage.removeLike(filmId, userId);
        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    @Cacheable(value = "popularFilms", key = "#count", unless = "#result == null || #result.isEmpty()")
    public Collection<Film> getPopular(Integer count) {
        if (count == null || count <= 0) {
            count = defaultPopularCount;
        }
        log.debug("Запрос популярных фильмов, count: {}", count);
        Collection<Film> films = likeStorage.getPopular(count);
        films.forEach(film -> film.setGenres(genreStorage.getGenresForFilm(film.getId())));
        return films;
    }

    private void validateFilmExists(Long id) {
        if (!filmStorage.exists(id)) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
    }

    private void validateUserExists(Long userId) {
        if (!userStorage.exists(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    private void validateName(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
    }

    private void validateDuration(Film film) {
        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность должна быть положительным числом");
        }
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() != null &&
                film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    private void validateMpa(Film film) {
        if (film.getMpaRating() != null && film.getMpaRating().getId() != null) {
            if (!mpaDbStorage.exists(film.getMpaRating().getId())) {
                throw new NotFoundException("Рейтинг с id " + film.getMpaRating().getId() + " не найден");
            }
        }
    }

    private void validateGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        Set<Integer> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (genreIds.isEmpty()) {
            return;
        }

        if (!genreStorage.existsAll(genreIds)) {
            throw new NotFoundException("Один или несколько жанров не найдены");
        }

    }
}
