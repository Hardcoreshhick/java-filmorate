package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

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
    private static final String SQL_EXISTS_GENRES_BY_IDS =
            "SELECT COUNT(*) FROM genres WHERE id IN (%s)";

    @Override
    public void addGenresToFilm(long filmId, Set<Genre> genres) {
       List<Object[]> batchArgs = new ArrayList<>();
       for (Genre genre : genres) {
           batchArgs.add(new Object[] { filmId, genre.getId() });
       }

       jdbc.batchUpdate(SQL_INSERT_FILM_GENRE, batchArgs);
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

    @Override
    public boolean existsAll(Set<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return true;
        }

        Set<Integer> validIds = genreIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (validIds.isEmpty()) {
            return true;
        }

        String ids = validIds.stream().map(Object::toString).collect(Collectors.joining(","));

        String sql = String.format(SQL_EXISTS_GENRES_BY_IDS, ids);

        Integer count = jdbc.queryForObject(sql, Integer.class);

        return count != null && count == validIds.size();
    }
}