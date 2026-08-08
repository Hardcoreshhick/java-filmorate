package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.util.TestDataFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserService тесты")
class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(new InMemoryUserStorage());
    }

    @Nested
    @DisplayName("Создание пользователя")
    class CreateUserTests {

        @Test
        @DisplayName("Если имя не указано, используется логин")
        void shouldSetNameFromLoginIfEmpty() {
            User user = TestDataFactory.createDefaultUser();
            user.setName(null);

            User created = userService.create(user);

            assertEquals(user.getLogin(), created.getName());
        }

        @Test
        @DisplayName("Создание с существующим email выбрасывает исключение")
        void shouldThrowIfEmailExists() {
            User user1 = TestDataFactory.createUser("user1", "test@mail.ru");
            userService.create(user1);

            User user2 = TestDataFactory.createUser("user2", "test@mail.ru");

            assertThrows(DuplicatedDataException.class, () -> userService.create(user2));
        }

    }

    @Nested
    @DisplayName("Обновление пользователя")
    class UpdateUserTests {

        private User existingUser;

        @BeforeEach
        void createUser() {
            existingUser = userService.create(TestDataFactory.createDefaultUser());
        }

        @Test
        @DisplayName("Обновление всех полей работает корректно")
        void shouldUpdateAllFields() {
            existingUser.setName("New Name");
            existingUser.setLogin("newLogin");

            User updated = userService.update(existingUser);

            assertEquals("New Name", updated.getName());
            assertEquals("newLogin", updated.getLogin());
        }

        @Test
        @DisplayName("Обновление несуществующего пользователя выбрасывает исключение")
        void shouldThrowIfNotFound() {
            User unknown = TestDataFactory.createDefaultUser();
            unknown.setId(999L);

            assertThrows(UserNotFoundException.class, () -> userService.update(unknown));
        }
    }

    @Nested
    @DisplayName("Работа с друзьями")
    class FriendTests {

        private User user1;
        private User user2;
        private User user3;

        @BeforeEach
        void createUsers() {
            user1 = userService.create(TestDataFactory.createUser("user1", "user1@mail.ru"));
            user2 = userService.create(TestDataFactory.createUser("user2", "user2@mail.ru"));
            user3 = userService.create(TestDataFactory.createUser("user3", "user3@mail.ru"));
        }

        @Test
        @DisplayName("Добавление друга — связь двусторонняя")
        void addFriend_shouldAddBidirectional() {
            userService.addFriend(user1.getId(), user2.getId());

            Set<User> friends1 = userService.getFriends(user1.getId());
            Set<User> friends2 = userService.getFriends(user2.getId());

            assertTrue(friends1.contains(user2));
            assertTrue(friends2.contains(user1));
        }

        @Test
        @DisplayName("Удаление друга — связь удаляется с обеих сторон")
        void removeFriend_shouldRemoveBidirectional() {
            userService.addFriend(user1.getId(), user2.getId());
            userService.removeFriend(user1.getId(), user2.getId());

            assertTrue(userService.getFriends(user1.getId()).isEmpty());
            assertTrue(userService.getFriends(user2.getId()).isEmpty());
        }

        @Test
        @DisplayName("Общие друзья — возвращаются только общие")
        void getCommonFriends_shouldReturnOnlyCommon() {
            userService.addFriend(user1.getId(), user2.getId());
            userService.addFriend(user1.getId(), user3.getId());
            userService.addFriend(user2.getId(), user3.getId());

            Set<User> common = userService.getCommonFriends(user1.getId(), user2.getId());

            assertEquals(1, common.size());
            assertTrue(common.contains(user3));
        }
    }
}