package ru.osipov.deploy.services;

import org.springframework.transaction.annotation.Transactional;
import ru.osipov.deploy.entities.Film;
import ru.osipov.deploy.models.FilmInfo;

import javax.annotation.Nonnull;
import java.util.List;

@Transactional
public interface FilmService {
    @Nonnull
    List<FilmInfo> getAllFilms();

    @Nonnull
    FilmInfo getByName(@Nonnull String name);


    @Nonnull
    List<FilmInfo> getByRating(Short rating);

    FilmInfo updateFilmRating(String fname, Short rating);

    FilmInfo deleteFilm(String name);

}
