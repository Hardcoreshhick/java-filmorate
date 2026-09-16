package ru.yandex.practicum.filmorate.storage.friendship;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("Интеграционные тесты FriendshipDbStorage")
@Sql(scripts = "classpath:data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import({FriendshipDbStorage.class, UserDbStorage.class, UserMapper.class})
class FriendshipDbStorageTest {

    private final FriendshipDbStorage friendshipStorage;
    private final UserDbStorage userStorage;

    private Long userId1;
    private Long userId2;

    @BeforeEach
    void setUp() {
        User user1 = new User();
        user1.setEmail("friend1@mail.ru");
        user1.setLogin("friend1");
        user1.setName("Friend One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("friend2@mail.ru");
        user2.setLogin("friend2");
        user2.setName("Friend Two");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        userId1 = userStorage.create(user1).getId();
        userId2 = userStorage.create(user2).getId();
    }

    @Test
    @DisplayName("Добавление друга")
    void testAddFriend() {
        friendshipStorage.addFriend(userId1, userId2);

        Collection<User> friends = friendshipStorage.getFriends(userId1);

        assertThat(friends).hasSize(1);
        assertThat(friends.iterator().next().getId()).isEqualTo(userId2);
    }

    @Test
    @DisplayName("Проверка существования дружбы (односторонняя)")
    void testIsFriend() {
        friendshipStorage.addFriend(userId1, userId2);

        assertThat(friendshipStorage.isFriend(userId1, userId2)).isTrue();
        assertThat(friendshipStorage.isFriend(userId2, userId1)).isFalse();
    }

    @Test
    @DisplayName("Удаление друга")
    void testRemoveFriend() {
        friendshipStorage.addFriend(userId1, userId2);
        friendshipStorage.removeFriend(userId1, userId2);

        Collection<User> friends = friendshipStorage.getFriends(userId1);
        assertThat(friends).isEmpty();
    }

    @Test
    @DisplayName("Получение общих друзей")
    void testGetCommonFriends() {
        User user3 = new User();
        user3.setEmail("friend3@mail.ru");
        user3.setLogin("friend3");
        user3.setName("Friend Three");
        user3.setBirthday(LocalDate.of(1992, 3, 3));
        Long userId3 = userStorage.create(user3).getId();

        friendshipStorage.addFriend(userId1, userId2);
        friendshipStorage.addFriend(userId1, userId3);
        friendshipStorage.addFriend(userId2, userId3);

        Collection<User> commonFriends = friendshipStorage.getCommonFriends(userId1, userId2);

        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.iterator().next().getId()).isEqualTo(userId3);
    }

    @Test
    @DisplayName("Получение списка друзей пользователя")
    void testGetFriends() {
        friendshipStorage.addFriend(userId1, userId2);

        Collection<User> friends = friendshipStorage.getFriends(userId1);

        assertThat(friends).hasSize(1);
        assertThat(friends.iterator().next().getId()).isEqualTo(userId2);
    }

    @Test
    @DisplayName("Попытка добавить самого себя в друзья должна выбросить исключение")
    void testAddSelfAsFriend_shouldThrowException() {
        assertThatThrownBy(() -> friendshipStorage.addFriend(userId1, userId1))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Попытка удалить несуществующую дружбу должна выбросить исключение")
    void testRemoveNonExistentFriend_shouldThrowException() {
        assertThatThrownBy(() -> friendshipStorage.removeFriend(userId1, userId2))
                .isInstanceOf(RuntimeException.class);
    }
}