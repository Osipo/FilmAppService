package ru.osipov.deploy.web;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.osipov.deploy.models.FilmInfo;
import ru.osipov.deploy.services.FilmService;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping("/v1/films")
public class FilmController {
    private final FilmService fService;

    private static final Logger logger = LoggerFactory.getLogger(FilmController.class);

    @Autowired
    public FilmController(FilmService fs){
        this.fService = fs;
    }

    
    //GET: /v1/films/{film_name}
    //If no any name was specified -> getAll() [GET: /v1/films, /v1/films/]
    @GetMapping(produces = APPLICATION_JSON_UTF8_VALUE, path={"/{name}","/"})
    public List<FilmInfo> getAllByName(@PathVariable(required = false, name= "name") String name){
        logger.info("Get by name");
        logger.info("/v1/films");
        List<FilmInfo> films;
        if(name == null || name.equals("")) {
            logger.info("Name was not specified. Get all.");
            films = fService.getAllFilms();
        }
        else {
            logger.info("/v1/films/'{}'",name);
            logger.info("Name is '{}'",name);
            films = new ArrayList<FilmInfo>();
            films.add(fService.getByName(name));
        }
        logger.info("Count: "+films.size());
        return films;
    }

    //GET: /v1/films?r='...'
    //If no any name was specified -> getAll() [GET: /v1/films]
    @GetMapping(produces = APPLICATION_JSON_UTF8_VALUE)
    public List<FilmInfo> getByRating(@RequestParam(required = false, name= "r") Short rating){
        logger.info("Get by rating");
        logger.info("/v1/films");
        List<FilmInfo> films;
        if(rating == null) {
            logger.info("Rating was not specified. Get all.");
            films = fService.getAllFilms();
        }
        else {
            logger.info("Rating = '{}'",rating);
            films = fService.getByRating(rating);
        }
        logger.info("Count: "+films.size());
        return films;
    }

    //POST: /v1/films/update/{film_name}?r='...'
    //if no parameter was specified -> badRequest()
    @PostMapping(produces = APPLICATION_JSON_UTF8_VALUE, path = {"/update/{fname}"})
    public ResponseEntity updateRating(@PathVariable(name = "fname") String fname, @RequestParam(required = true,name = "r",defaultValue = "-1") Short rating){
        logger.info("/v1/films/update/");
        logger.info("Rating = '{}'",rating);
        FilmInfo f = null;
        try{
            f = fService.updateFilmRating(fname,rating);
        }
        catch (IllegalStateException e){
//            logger.info("Return json object with error message...");
//            JsonObject obj = new JsonObject();
//            obj.addProperty("error",e.getMessage());
            String v = e.getMessage();
            if(v.contains("not exist")){
                return ResponseEntity.notFound().build();
            }
            else{
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.ok(f);
    }

    //POST: /v1/films/delete/{film_name}
    //if no parameter was specified -> badRequest()
    @PostMapping(produces = APPLICATION_JSON_UTF8_VALUE,path = "/delete/{fname}")
    public ResponseEntity deleteFilm(@PathVariable(name = "fname")String name){
        logger.info("/v1/films/delete/'{}'",name);
        FilmInfo f = null;
        try{
            f = fService.deleteFilm(name);
        }
        catch (IllegalStateException e){
            logger.info("Return 404 not found.");
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(f);
    }
}
