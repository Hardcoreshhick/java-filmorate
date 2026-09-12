package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaService mpaService;

    @GetMapping
    public List<MpaRating> findAll() {
        log.debug("Запрос на получение всех рейтингов");
        return mpaService.findAll();
    }

    @GetMapping("/{id}")
    public MpaRating findById(@PathVariable int id) {
        log.debug("Запрос на получение рейтинга с id={}", id);
        return mpaService.findById(id);
    }
}
