package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;

import java.sql.PreparedStatement;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Primary
@Repository
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final FilmMapper filmMapper;
    private final GenreStorage genreStorage;   // ← делегируем жанры
    private final LikeStorage likeStorage;     // ← делегируем лайки

    private static final String SQL_INSERT_FILM =
            "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE_FILM =
            "UPDATE films SET name=?, description=?, release_date=?, duration=?, mpa_id=? WHERE id=?";
    private static final String SQL_SELECT_FILM_BY_ID =
            "SELECT f.*, m.name as mpa_name FROM films f LEFT JOIN mpa m ON f.mpa_id = m.id WHERE f.id = ?";
    private static final String SQL_SELECT_ALL_FILMS =
            "SELECT f.*, m.name as mpa_name FROM films f LEFT JOIN mpa m ON f.mpa_id = m.id ORDER BY f.id";
    private static final String SQL_DELETE_FILM =
            "DELETE FROM films WHERE id = ?";
    private static final String SQL_EXISTS_FILM =
            "SELECT COUNT(*) FROM films WHERE id = ?";

    @Transactional
    @Override
    public Film create(Film film) {
        log.debug("Создание фильма {}", film.getName());
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT_FILM, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            Integer mpaId = film.getMpaRating() != null ? film.getMpaRating().getId() : null;
            if (mpaId != null) {
                ps.setInt(5, mpaId);
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        log.info("Создан фильм с id {}", film.getId());

        genreStorage.addGenresToFilm(film.getId(), film.getGenres());
        return film;
    }

    @Transactional
    @Override
    public Film update(Film film) {
        log.debug("Обновление фильма с id: {}", film.getId());

        int rowsUpdated = jdbc.update(SQL_UPDATE_FILM,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaRating() != null ? film.getMpaRating().getId() : null,
                film.getId()
        );

        if (rowsUpdated == 0) {
            log.warn("Фильм с id {} не найден", film.getId());
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        if (film.getGenres() != null) {
            genreStorage.deleteGenresFromFilm(film.getId());
            if (!film.getGenres().isEmpty()) {
                genreStorage.addGenresToFilm(film.getId(), film.getGenres());
            }
        }

        log.info("Обновлён фильм с id: {}", film.getId());
        return film;
    }

    @Override
    public Optional<Film> findById(long id) {
        log.debug("Поиск фильма по id: {}", id);
        List<Film> films = jdbc.query(SQL_SELECT_FILM_BY_ID, filmMapper, id);
        if (films.isEmpty()) {
            return Optional.empty();
        }
        Film film = films.getFirst();
        film.setGenres(genreStorage.getGenresForFilm(id));
        return Optional.of(film);
    }

    @Override
    public Collection<Film> findAll() {
        log.debug("Запрос всех фильмов");
        List<Film> films = jdbc.query(SQL_SELECT_ALL_FILMS, filmMapper);
        films.forEach(film -> film.setGenres(genreStorage.getGenresForFilm(film.getId())));
        return films;
    }

    @Transactional
    @Override
    public void delete(long id) {
        log.debug("Удаление фильма с id: {}", id);
        int rowsDeleted = jdbc.update(SQL_DELETE_FILM, id);
        if (rowsDeleted == 0) {
            log.warn("Фильм с id {} не найден для удаления", id);
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        log.info("Удалён фильм с id: {}", id);
    }

    @Override
    public boolean exists(long id) {
        log.debug("Проверка существования фильма с id: {}", id);
        Integer count = jdbc.queryForObject(SQL_EXISTS_FILM, Integer.class, id);
        return count > 0;
    }

    public void addLike(long filmId, long userId) {
        likeStorage.addLike(filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        likeStorage.removeLike(filmId, userId);
    }

    public Collection<Film> getPopular(int count) {
        return likeStorage.getPopular(count);
    }
}
