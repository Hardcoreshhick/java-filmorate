package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты валидации пользователей")
class UserValidationTest {

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

    private User validUser() {
        User user = new User();
        user.setEmail("john@example.com");
        user.setLogin("john_doe");
        user.setName("John Doe");
        user.setBirthday(LocalDate.of(1990, 5, 15));
        return user;
    }

    @Test
    @DisplayName("Корректный пользователь проходит валидацию")
    void validUser_shouldPassValidation() {
        User user = validUser();

        var violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Корректный пользователь не должен иметь нарушений");
    }

    @Test
    @DisplayName("Email обязателен и не может быть пустым")
    void blankEmail_shouldFailValidation() {
        User user = validUser();
        user.setEmail("");

        var violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertEquals("Email не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Email должен иметь корректный формат")
    void invalidEmailFormat_shouldFailValidation() {
        User user = validUser();
        user.setEmail("invalid-email");

        var violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertEquals("Email должен быть корректным", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Email может содержать символ +")
    void emailWithPlus_shouldPassValidation() {
        User user = validUser();
        user.setEmail("john+test@example.com");

        var violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Email с + должен быть допустим");
    }

    @Test
    @DisplayName("Логин обязателен и не может быть пустым")
    void blankLogin_shouldFailValidation() {
        User user = validUser();
        user.setLogin("");

        var violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertEquals("Логин не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Логин не может содержать пробелы")
    void loginWithSpaces_shouldFailValidation() {
        User user = validUser();
        user.setLogin("john doe");

        var violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertEquals("Логин не должен содержать пробелы", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Логин может содержать символ подчёркивания")
    void loginWithUnderscore_shouldPassValidation() {
        User user = validUser();
        user.setLogin("john_doe");

        var violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Логин с _ должен быть допустим");
    }

    @Test
    @DisplayName("Логин может содержать дефис")
    void loginWithHyphen_shouldPassValidation() {
        User user = validUser();
        user.setLogin("john-doe");

        var violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Логин с - должен быть допустим");
    }

    @Test
    @DisplayName("Имя может быть null (будет использован логин)")
    void nullName_shouldPassValidation() {
        User user = validUser();
        user.setName(null);

        var violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Имя null должно быть допустимо");
    }

    @Test
    @DisplayName("Имя может быть пустым (будет использован логин)")
    void emptyName_shouldPassValidation() {
        User user = validUser();
        user.setName("");

        var violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Пустое имя должно быть допустимо");
    }

    @Test
    @DisplayName("Дата рождения не может быть в будущем")
    void birthdayInFuture_shouldFailValidation() {
        User user = validUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        var violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertEquals("Дата рождения не может быть в будущем", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Дата рождения сегодня допустима")
    void birthdayToday_shouldPassValidation() {
        User user = validUser();
        user.setBirthday(LocalDate.now());

        var violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Дата рождения сегодня должна быть допустима");
    }

    @Test
    @DisplayName("Дата рождения в далёком прошлом допустима")
    void birthdayInPast_shouldPassValidation() {
        User user = validUser();
        user.setBirthday(LocalDate.of(1900, 1, 1));

        var violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Дата рождения в прошлом должна быть допустима");
    }

    @Test
    @DisplayName("При нескольких ошибках возвращается первое нарушение")
    void multipleInvalidFields_shouldReturnFirstViolation() {
        User user = new User();
        user.setEmail("invalid");
        user.setLogin("test user");
        user.setName(null);
        user.setBirthday(LocalDate.now().plusDays(1));

        var violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        String firstMessage = violations.iterator().next().getMessage();
        assertTrue(firstMessage.equals("Email должен быть корректным") ||
                        firstMessage.equals("Дата рождения не может быть в будущем"),
                "Первая ошибка должна быть либо о email, либо о дате рождения");
    }
}