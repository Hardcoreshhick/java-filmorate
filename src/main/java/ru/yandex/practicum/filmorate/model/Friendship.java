package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.Instant;

@Data
public class Friendship {
    private Long userId;
    private Long friendId;
    private FriendshipStatus status;
    private Instant createdAt;
}
