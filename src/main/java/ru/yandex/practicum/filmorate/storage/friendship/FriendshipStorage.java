package ru.yandex.practicum.filmorate.storage.friendship;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface FriendshipStorage {

    void addFriend(long userId, long friendId);

    void removeFriend(long userId, long friendId);

    Collection<User> getFriends(long userId);

    Collection<User> getCommonFriends(long userId, long otherId);

    boolean isFriend(long userId, long friendId);

    void confirmFriend(long userId, long friendId);

    Collection<User> getPendingFriends(long userId);
}