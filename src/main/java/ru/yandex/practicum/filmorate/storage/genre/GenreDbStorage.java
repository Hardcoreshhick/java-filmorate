package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbc;
    private final GenreMapper genreMapper;

    private static final String SQL_INSERT_FILM_GENRE =
            "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String SQL_DELETE_FILM_GENRES =
            "DELETE FROM film_genre WHERE film_id = ?";
    private static final String SQL_SELECT_GENRES_BY_FILM =
            "SELECT g.* FROM genres g JOIN film_genre fg ON g.id = fg.genre_id WHERE fg.film_id = ? ORDER BY g.id";
    private static final String SQL_SELECT_ALL_GENRES =
            "SELECT * FROM genres ORDER BY id";
    private static final String SQL_SELECT_GENRE_BY_ID =
            "SELECT * FROM genres WHERE id = ?";
    private static final String SQL_EXISTS_GENRE =
            "SELECT COUNT(1) FROM genres WHERE id = ?";

    @Override
    public void addGenresToFilm(long filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) return;
        new HashSet<>(genres)
                .forEach(genre -> jdbc.update(SQL_INSERT_FILM_GENRE, filmId, genre.getId()));
        log.debug("Добавлены жанры для фильма id: {}", filmId);
    }

    @Override
    public void deleteGenresFromFilm(long filmId) {
        jdbc.update(SQL_DELETE_FILM_GENRES, filmId);
        log.debug("Удалены жанры для фильма id: {}", filmId);
    }

    @Override
    public Set<Genre> getGenresForFilm(long filmId) {
        return new HashSet<>(jdbc.query(SQL_SELECT_GENRES_BY_FILM, genreMapper, filmId));
    }

    @Override
    public List<Genre> findAll() {
        log.debug("Запрос всех жанров");
        return jdbc.query(SQL_SELECT_ALL_GENRES, genreMapper);
    }

    @Override
    public Optional<Genre> findById(int id) {
        log.debug("Поиск жанра по id: {}", id);
        List<Genre> result = jdbc.query(SQL_SELECT_GENRE_BY_ID, genreMapper, id);
        return result.stream().findFirst();
    }

    @Override
    public boolean exists(int id) {
        log.debug("Проверка существования жанра с id: {}", id);
        Integer count = jdbc.queryForObject(SQL_EXISTS_GENRE, Integer.class, id);
        return count > 0;
    }
}