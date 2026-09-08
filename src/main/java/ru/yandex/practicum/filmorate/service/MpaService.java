package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaStorage mpaStorage;

    public List<MpaRating> findAll() {
        log.debug("Запрос всех рейтингов");
        List<MpaRating> ratings = mpaStorage.findAll();
        log.debug("Найдено {} рейтингов", ratings.size());
        return ratings;
    }

    public MpaRating findById(int id) {
        log.debug("Поиск рейтинга по id: {}", id);
        return mpaStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id=" + id + " не найден"));

    }
}
