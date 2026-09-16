package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GenreStorage {
    void addGenresToFilm(long filmId, Set<Genre> genres);

    void deleteGenresFromFilm(long filmId);

    Set<Genre> getGenresForFilm(long filmId);

    List<Genre> findAll();

    Optional<Genre> findById(int id);

    boolean exists(int id);

    boolean existsAll(Set<Integer> genreIds);
}
