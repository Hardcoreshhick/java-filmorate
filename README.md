# Filmorate

Социальная сеть для киноманов: ставь оценки, добавляй друзей и следи за рейтингом фильмов.

## Схема базы данных

![ER-диаграмма](docs/schema.png)

## Технологии

- Java 21
- Spring Boot 3
- PostgreSQL
- JPA / Hibernate

## Функциональность

- Создание, обновление, удаление пользователей и фильмов
- Добавление друзей с подтверждением
- Лайки фильмов
- Топ-10 популярных фильмов
- Общие друзья

## Примеры запросов

# Получить все фильмы с жанрами и рейтингом

```sql
SELECT 
    f.*,
    m.name AS mpa_rating,
    STRING_AGG(g.name, ', ') AS genres
FROM films f
LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
LEFT JOIN film_genre fg ON f.id = fg.film_id
LEFT JOIN genres g ON fg.genre_id = g.id
GROUP BY f.id, m.name;
```

## Топ-10 популярных фильмов по лайкам

```sql
SELECT 
    f.id,
    f.name,
    COUNT(l.user_id) AS likes_count
FROM films f
LEFT JOIN likes l ON f.id = l.film_id
GROUP BY f.id
ORDER BY likes_count DESC
LIMIT 10;
```

## Получить друзей пользователя (подтверждённых)

```sql
SELECT u.*
FROM friendship f
JOIN users u ON f.friend_id = u.id
WHERE f.user_id = 1 AND f.status = 'CONFIRMED';
```