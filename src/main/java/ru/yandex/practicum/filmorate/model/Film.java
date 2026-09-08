package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.MinimumDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Long id;

    @NotBlank(message = "Названия не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описания не может быть длиннее 200 символов")
    private String description;

    @MinimumDate()
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность должна быть указана")
    @Positive(message = "Продолжительность должна быть положительным числом")
    private Integer duration;

    private Set<Genre> genres = new HashSet<>();
    @JsonProperty("mpa")
    private MpaRating mpaRating;
}
