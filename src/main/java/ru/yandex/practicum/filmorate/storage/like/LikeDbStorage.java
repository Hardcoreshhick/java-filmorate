package ru.yandex.practicum.filmorate.storage.like;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LikeDbStorage implements LikeStorage {

    private final JdbcTemplate jdbc;
    private final FilmMapper filmMapper;

    private static final String SQL_INSERT_LIKE =
            "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String SQL_DELETE_LIKE =
            "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String SQL_SELECT_POPULAR =
            "SELECT f.*, m.name as mpa_name, COUNT(l.user_id) as likes_count " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "LEFT JOIN likes l ON f.id = l.film_id " +
                    "GROUP BY f.id " +
                    "ORDER BY likes_count DESC, f.id " +
                    "LIMIT ?";

    @Override
    public void addLike(long filmId, long userId) {
        log.debug("Пользователь {} ставит лайк фильму {}", userId, filmId);
        jdbc.update(SQL_INSERT_LIKE, filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        log.debug("Пользователь {} убирает лайк с фильма {}", userId, filmId);
        int rowsDeleted = jdbc.update(SQL_DELETE_LIKE, filmId, userId);
        if (rowsDeleted == 0) {
            log.warn("Лайк от пользователя {} для фильма {} не найден", userId, filmId);
            throw new NotFoundException("Лайк от пользователя " + userId + " для фильма " + filmId + " не найден");
        }
        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    @Override
    public Collection<Film> getPopular(int count) {
        if (count <= 0) {
            log.warn("Запрошено невалидное количество популярных фильмов: {}", count);
            return Collections.emptyList();
        }
        log.debug("Запрос популярных фильмов, count: {}", count);
        List<Film> films = jdbc.query(SQL_SELECT_POPULAR, filmMapper, count);
        log.debug("Найдено {} популярных фильмов", films.size());
        return films;
    }
}