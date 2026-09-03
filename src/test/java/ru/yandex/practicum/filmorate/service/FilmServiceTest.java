package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.util.TestDataFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("FilmService тесты")
class FilmServiceTest {

    private FilmService filmService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(new InMemoryUserStorage());
        filmService = new FilmService(new InMemoryFilmStorage(), userService);
    }

    @Nested
    @DisplayName("Создание фильма")
    class CreateFilmTests {

        @Test
        @DisplayName("Создание с пустым названием выбрасывает исключение")
        void shouldThrowIfNameIsBlank() {
            Film film = TestDataFactory.createDefaultFilm();
            film.setName("");

            assertThrows(ValidationException.class, () -> filmService.create(film));
        }
    }

    @Nested
    @DisplayName("Обновление фильма")
    class UpdateFilmTests {

        private Film existingFilm;

        @BeforeEach
        void createFilm() {
            existingFilm = filmService.create(TestDataFactory.createDefaultFilm());
        }

        @Test
        @DisplayName("Обновление полей работает корректно")
        void shouldUpdateFields() {
            existingFilm.setName("New Name");
            existingFilm.setDescription("New Description");

            Film updated = filmService.update(existingFilm);

            assertEquals("New Name", updated.getName());
            assertEquals("New Description", updated.getDescription());
        }

        @Test
        @DisplayName("Обновление несуществующего фильма выбрасывает исключение")
        void shouldThrowIfNotFound() {
            Film unknown = TestDataFactory.createDefaultFilm();
            unknown.setId(999L);

            assertThrows(NotFoundException.class, () -> filmService.update(unknown));
        }
    }

    @Nested
    @DisplayName("Лайки и популярные")
    class LikeTests {

        private User user1;
        private User user2;
        private User user3;
        private Film film1;
        private Film film2;
        private Film film3;

        @BeforeEach
        void createData() {
            user1 = userService.create(TestDataFactory.createUser("user1", "user1@mail.ru"));
            user2 = userService.create(TestDataFactory.createUser("user2", "user2@mail.ru"));
            user3 = userService.create(TestDataFactory.createUser("user3", "user3@mail.ru"));

            film1 = filmService.create(TestDataFactory.createFilm("Film1", 120));
            film2 = filmService.create(TestDataFactory.createFilm("Film2", 130));
            film3 = filmService.create(TestDataFactory.createFilm("Film3", 140));
        }

        @Test
        @DisplayName("Лайк увеличивает количество")
        void addLike_shouldIncreaseCount() {
            filmService.addLike(film1.getId(), user1.getId());

            assertEquals(1, filmService.getLikesCount(film1.getId()));
        }

        @Test
        @DisplayName("Удаление лайка уменьшает количество")
        void removeLike_shouldDecreaseCount() {
            filmService.addLike(film1.getId(), user1.getId());
            filmService.removeLike(film1.getId(), user1.getId());

            assertEquals(0, filmService.getLikesCount(film1.getId()));
        }

        @Test
        @DisplayName("Популярные фильмы — сортировка по лайкам")
        void getPopularFilms_shouldReturnSorted() {
            filmService.addLike(film1.getId(), user1.getId());
            filmService.addLike(film1.getId(), user2.getId());
            filmService.addLike(film1.getId(), user3.getId());

            filmService.addLike(film2.getId(), user1.getId());
            filmService.addLike(film2.getId(), user2.getId());

            filmService.addLike(film3.getId(), user1.getId());

            List<Film> popular = filmService.getPopularFilms(3);

            assertEquals(film1.getId(), popular.get(0).getId());
            assertEquals(film2.getId(), popular.get(1).getId());
            assertEquals(film3.getId(), popular.get(2).getId());
        }
    }
}
