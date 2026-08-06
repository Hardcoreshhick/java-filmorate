package ru.yandex.practicum.filmorate.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.yandex.practicum.filmorate.validator.MinimumDateValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MinimumDateValidator.class)
@SuppressWarnings("unused")
public @interface MinimumDate {
    String message() default "Дата не может быть раньше указанной даты";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String value() default "1895-12-28";
}
