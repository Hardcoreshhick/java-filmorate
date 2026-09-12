package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbc;
    private final MpaMapper mpaMapper;

    private static final String SQL_SELECT_ALL_MPA = "SELECT * FROM mpa ORDER BY id";
    private static final String SQL_SELECT_MPA_BY_ID = "SELECT * FROM mpa WHERE id = ?";
    private static final String SQL_EXISTS_MPA = "SELECT Count(1) FROM mpa WHERE id = ?";

    @Override
    public List<MpaRating> findAll() {
        log.debug("Запрос всех рейтингов");
        return jdbc.query(SQL_SELECT_ALL_MPA, mpaMapper);
    }

    @Override
    public Optional<MpaRating> findById(int id) {
        log.debug("Поиск рейтинга по id: {}", id);
        List<MpaRating> result = jdbc.query(SQL_SELECT_MPA_BY_ID, mpaMapper, id);
        return result.stream().findFirst();
    }

    @Override
    public boolean exists(int id) {
        log.debug("Проверка существования рейтинга с id: {}", id);
        Integer count = jdbc.queryForObject(SQL_EXISTS_MPA, Integer.class, id);
        return count > 0;
    }
}
