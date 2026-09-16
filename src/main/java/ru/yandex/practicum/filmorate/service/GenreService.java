package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreStorage genreStorage;

    @Cacheable(value = "genres", key = "'all'")
    public List<Genre> findAll() {
        log.debug("Запрос всех жанров");
        List<Genre> genres = genreStorage.findAll();
        log.info("Найдено {} жанров", genres.size());
        log.info("Найдено {} жанров", genres.size());
        return List.copyOf(genres);
    }

    public Genre findById(int id) {
        log.debug("Поиск жанра по id {}", id);
        return genreStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id=" + id + " не найден"));
    }
}
