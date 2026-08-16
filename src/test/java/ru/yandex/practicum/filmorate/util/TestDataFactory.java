package ru.yandex.practicum.filmorate.util;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public class TestDataFactory {
    public static User createDefaultUser() {
        User user = new User();
        user.setLogin("defaultLogin");
        user.setEmail("default@mail.ru");
        user.setName("Default Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    public static User createUser(String login, String email) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(email);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    public static Film createDefaultFilm() {
        Film film = new Film();
        film.setName("Default Film");
        film.setDescription("Default Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }

    public static Film createFilm(String name, int duration) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description for " + name);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(duration);
        return film;
    }
}
