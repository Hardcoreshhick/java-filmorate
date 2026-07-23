package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.MinimumDate;

import java.time.LocalDate;

@Data
public class Film {
    private Long id;

    @NotBlank(message = "Названия не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описания не может быть длиннее 200 символов")
    private String description;

    @MinimumDate
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность должна быть указана")
    @Positive(message = "Продолжительность должна быть положительным числом")
    private Integer duration;
}
