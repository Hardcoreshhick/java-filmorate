package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("Интеграционные тесты FilmDbStorage")
@Sql(scripts = "classpath:data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import({
        FilmDbStorage.class,
        FilmMapper.class,
        GenreDbStorage.class,
        GenreMapper.class,      // ← ДОБАВИТЬ!
        LikeDbStorage.class
})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpaRating(mpa);

        film.setGenres(new HashSet<>());

        return film;
    }

    @Test
    @DisplayName("Создание фильма")
    void testCreateFilm() {
        Film film = createTestFilm();
        Film created = filmStorage.create(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Test Film");
    }

    @Test
    @DisplayName("Поиск фильма по id")
    void testFindFilmById() {
        Film film = createTestFilm();
        Film created = filmStorage.create(film);

        Optional<Film> found = filmStorage.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(created.getId());
    }

    @Test
    @DisplayName("Получение всех фильмов")
    void testFindAllFilms() {
        Film film = createTestFilm();
        filmStorage.create(film);

        assertThat(filmStorage.findAll()).isNotEmpty();
    }

    @Test
    @DisplayName("Обновление фильма")
    void testUpdateFilm() {
        Film film = createTestFilm();
        Film created = filmStorage.create(film);

        created.setName("Updated Film");
        filmStorage.update(created);

        Optional<Film> found = filmStorage.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated Film");
    }

    @Test
    @DisplayName("Удаление фильма")
    void testDeleteFilm() {
        Film film = createTestFilm();
        Film created = filmStorage.create(film);

        filmStorage.delete(created.getId());

        assertThat(filmStorage.findById(created.getId())).isEmpty();
    }

    @Test
    @DisplayName("Проверка существования фильма")
    void testExistsFilm() {
        Film film = createTestFilm();
        Film created = filmStorage.create(film);

        assertThat(filmStorage.exists(created.getId())).isTrue();
        assertThat(filmStorage.exists(999L)).isFalse();
    }
}