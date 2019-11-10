package ru.osipov.deploy.services;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.osipov.deploy.entities.Film;
import ru.osipov.deploy.models.FilmInfo;
import ru.osipov.deploy.repositories.FilmRepository;

import javax.annotation.Nonnull;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.boot.system.SystemProperties.get;

@Service
public class FilmServiceImpl implements FilmService {

    private final FilmRepository rep;

    private static final Logger logger = getLogger(FilmServiceImpl.class);

    @Autowired
    public FilmServiceImpl(FilmRepository r){
        this.rep = r;
    }

    @Nonnull
    @Transactional(readOnly = true)
    @Override
    public List<FilmInfo> getAllFilms() {
        return rep.findAll().stream().map(this::buildModel).collect(Collectors.toList());
    }

    @Nonnull
    @Transactional(readOnly = true)
    @Override
    public FilmInfo getByName(@Nonnull String name) {
        logger.info("Get film by name = '{}'",name);
        Optional<FilmInfo> o =  rep.findByFname(name).map(this::buildModel);
        return o.orElseGet(() -> new FilmInfo(-1l, "", (short)-1,-1l));
    }

    @Nonnull
    @Transactional(readOnly = true)
    @Override
    public List<FilmInfo> getByRating(@Nonnull Short rating) {
        logger.info("Get films by rating = '{}'",rating);
        return rep.findByRating(rating).stream().map(this::buildModel).collect(Collectors.toList());
    }


    @Override
    @Transactional
    public FilmInfo updateFilmRating(String name, Short rating) throws IllegalStateException{
        Optional<Film> o = rep.findByFname(name);
        if(o.isPresent() && rating >= 0 && rating <= 100){
            logger.info("Update rating of the film = '{}'",name);
            Film f = o.get();
            f.setRating(rating);
            rep.save(f);
            logger.info("Updated successful.  New rating = '{}'",f.getRating());
            return buildModel(f);
        }
        else if(rating > 100 || rating < 0){
            logger.info("Illegal rating value '{}'",rating);
            throw new IllegalStateException("Illegal rating value. Must be[0..100]");
        }
        else{
            logger.info("not found film with name = '{}'",name);
            throw new IllegalStateException("Film with name"+name+"does not exist");
        }
    }

    @Override
    @Transactional
    public FilmInfo deleteFilm(String name) throws IllegalStateException{
        logger.info("Delete film by name = '{}'",name);
        Optional<Film> o = rep.findByFname(name);
        if(o.isPresent()){
            logger.info("Film was found.");
            FilmInfo r = buildModel(o.get());
            rep.delete(o.get());
            logger.info("Deleted successful.");
            return r;
        }
        else{
            logger.info("Film was not found.");
            throw new IllegalStateException("Film with name"+name+"was not found.");
        }
    }


    @Nonnull
    private FilmInfo buildModel(@Nonnull Film fi) {
        logger.info("Cinema: '{}'",fi);
        return new FilmInfo(fi.getFid(),fi.getFname(), fi.getRating(),fi.getGid());
    }
}
