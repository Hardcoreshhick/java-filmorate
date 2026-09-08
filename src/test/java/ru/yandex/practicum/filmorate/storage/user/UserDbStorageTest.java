package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("Интеграционные тесты UserDbStorage")
@Sql(scripts = "classpath:data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import({UserDbStorage.class, UserMapper.class, FriendshipDbStorage.class})
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    @DisplayName("Создание пользователя")
    void testCreateUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("test@mail.ru");
        assertThat(created.getLogin()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Поиск пользователя по id")
    void testFindUserById() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);

        Optional<User> found = userStorage.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(created.getId());
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void testFindAllUsers() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        userStorage.create(user);

        assertThat(userStorage.findAll()).isNotEmpty();
    }

    @Test
    @DisplayName("Обновление пользователя")
    void testUpdateUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);

        created.setName("Updated Name");
        userStorage.update(created);

        Optional<User> found = userStorage.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName("Удаление пользователя")
    void testDeleteUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);

        userStorage.delete(created.getId());

        assertThat(userStorage.findById(created.getId())).isEmpty();
    }

    @Test
    @DisplayName("Проверка существования пользователя")
    void testExistsUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);

        assertThat(userStorage.exists(created.getId())).isTrue();
        assertThat(userStorage.exists(999L)).isFalse();
    }

    @Test
    @DisplayName("Добавление друга (через UserDbStorage)")
    void testAddFriend() {
        User user1 = new User();
        user1.setEmail("user1@mail.ru");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@mail.ru");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        User created1 = userStorage.create(user1);
        User created2 = userStorage.create(user2);

        assertThat(userStorage.findById(created1.getId())).isPresent();
        assertThat(userStorage.findById(created2.getId())).isPresent();
    }
}