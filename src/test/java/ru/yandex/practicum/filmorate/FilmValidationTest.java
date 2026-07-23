package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты валидации фильмов")
class FilmValidationTest {

    private static ValidatorFactory factory;
    private Validator validator;

    @BeforeAll
    static void setUpAll() {
        factory = Validation.buildDefaultValidatorFactory();
    }

    @AfterAll
    static void tearDownAll() {
        if (factory != null) {
            factory.close();
        }
    }

    @BeforeEach
    void setUp() {
        validator = factory.getValidator();
    }

    private Film validFilm() {
        Film film = new Film();
        film.setName("Матрица");
        film.setDescription("Программист узнаёт правду о реальности");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136);
        return film;
    }

    @Test
    @DisplayName("Корректный фильм проходит валидацию")
    void validFilm_shouldPassValidation() {
        Film film = validFilm();

        var violations = validator.validate(film);

        assertTrue(violations.isEmpty(), "Корректный фильм не должен иметь нарушений");
    }

    @Test
    @DisplayName("Название фильма обязательно и не может быть пустым")
    void blankName_shouldFailValidation() {
        Film film = validFilm();
        film.setName("");

        var violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertEquals("Названия не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Название только из пробелов недопустимо")
    void nameWithSpacesOnly_shouldFailValidation() {
        Film film = validFilm();
        film.setName("   ");

        var violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertEquals("Названия не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Описание не может быть длиннее 200 символов")
    void descriptionExceedsMaxLength_shouldFailValidation() {
        Film film = validFilm();
        film.setDescription("А".repeat(201));

        var violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertEquals("Описания не может быть длиннее 200 символов", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Описание длиной ровно 200 символов допустимо")
    void descriptionWithMaxLength_shouldPassValidation() {
        Film film = validFilm();
        film.setDescription("А".repeat(200));

        var violations = validator.validate(film);

        assertTrue(violations.isEmpty(), "Описание из 200 символов должно быть допустимо");
    }

    @Test
    @DisplayName("Описание может быть null")
    void nullDescription_shouldPassValidation() {
        Film film = validFilm();
        film.setDescription(null);

        var violations = validator.validate(film);

        assertTrue(violations.isEmpty(), "Пустое описание должно быть допустимо");
    }

    @Test
    @DisplayName("Дата релиза не может быть раньше 28 декабря 1895 года")
    void releaseDateBefore1895_shouldFailValidation() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        var violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertEquals("Дата не может быть раньше 28 декабря 1895 года",
                violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Дата релиза ровно 28 декабря 1895 года допустима")
    void releaseDateExactly1895_shouldPassValidation() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));

        var violations = validator.validate(film);

        assertTrue(violations.isEmpty(), "Дата 28 декабря 1895 должна быть допустима");
    }

    @Test
    @DisplayName("Дата релиза может быть в будущем")
    void releaseDateInFuture_shouldPassValidation() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.now().plusYears(1));

        var violations = validator.validate(film);

        assertTrue(violations.isEmpty(), "Дата в будущем должна быть допустима");
    }

    @Test
    @DisplayName("Продолжительность должна быть положительным числом")
    void nonPositiveDuration_shouldFailValidation() {
        Film film = validFilm();
        film.setDuration(0);

        var violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertEquals("Продолжительность должна быть положительным числом",
                violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Продолжительность не может быть null")
    void nullDuration_shouldFailValidation() {
        Film film = validFilm();
        film.setDuration(null);

        var violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertEquals("Продолжительность должна быть указана", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("При нескольких ошибках возвращается первое нарушение")
    void multipleInvalidFields_shouldReturnFirstViolation() {
        Film film = new Film();
        film.setName("");
        film.setDescription("А".repeat(201));
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        film.setDuration(-10);

        var violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Должна быть хотя бы одна ошибка валидации");
    }
}