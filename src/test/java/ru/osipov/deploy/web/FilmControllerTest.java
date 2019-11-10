package ru.osipov.deploy.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ru.osipov.deploy.models.FilmInfo;
import ru.osipov.deploy.services.FilmService;

import java.util.ArrayList;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.when;
import static ru.osipov.deploy.TestParams.*;
@ExtendWith(SpringExtension.class)
@WebMvcTest(FilmController.class)
@AutoConfigureMockMvc
public class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private Gson gson = new GsonBuilder().create();
    private static final Logger logger = LoggerFactory.getLogger(FilmControllerTest.class);

    @MockBean
    FilmService fs;

    @Test
    void testAll() throws Exception {
        logger.info("testAll");
        List<FilmInfo> l = new ArrayList<>();
        l.add(new FilmInfo(1L,PARAMS1[0],Short.parseShort(PARAMS1[1]),2L));
        l.add(new FilmInfo(2L,PARAMS2[0],Short.parseShort(PARAMS2[1]),1L));
        l.add(new FilmInfo(3L,PARAMS3[0],Short.parseShort(PARAMS3[1]),-1L));
        when(fs.getAllFilms()).thenReturn(l);

        mockMvc.perform(get("/v1/films/").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value(PARAMS1[0]))
                .andExpect(jsonPath("$[0].rating").value(25))
                .andExpect(jsonPath("$[0].gid").value(2L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value(PARAMS2[0]))
                .andExpect(jsonPath("$[1].rating").value(18))
                .andExpect(jsonPath("$[1].gid").value(1L))
                .andExpect(jsonPath("$[2].id").value(3L))
                .andExpect(jsonPath("$[2].name").value(PARAMS3[0]))
                .andExpect(jsonPath("$[2].rating").value(16))
                .andExpect(jsonPath("$[2].gid").value(-1L));
    }

    @Test
    void testByName() throws Exception {
        logger.info("testByName");
        final List<FilmInfo> emt = new ArrayList<>();
        final FilmInfo f = new FilmInfo(111L,"MYST",(short)100,-1L);
        when(fs.getAllFilms()).thenReturn(emt);
        when(fs.getByName("MYST")).thenReturn(f);

        mockMvc.perform(get("/v1/films/MYST").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(111L))
                .andExpect(jsonPath("$[0].name").value("MYST"))
                .andExpect(jsonPath("$[0].rating").value(100))
                .andExpect(jsonPath("$[0].gid").value(-1L));

        mockMvc.perform(get("/v1/films/").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/v1/films").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").isEmpty());

    }

    @Test
    void testByRating() throws Exception {
        logger.info("testByRating");
        final List<FilmInfo> emt = new ArrayList<>();
        final List<FilmInfo> r = new ArrayList<>();
        r.add(new FilmInfo(55L,PARAMS1[0],Short.parseShort(PARAMS1[1]),12L));
        when(fs.getAllFilms()).thenReturn(emt);
        when(fs.getByRating((short)25)).thenReturn(r);

        mockMvc.perform(get("/v1/films?r=25").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(55L))
                .andExpect(jsonPath("$[0].name").value(PARAMS1[0]))
                .andExpect(jsonPath("$[0].rating").value(25))
                .andExpect(jsonPath("$[0].gid").value(12L));
        mockMvc.perform(get("/v1/films?r").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/v1/films?r=").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").isEmpty());

    }

    @Test
    void testUpdateRating() throws Exception {
        logger.info("testUpdateRating");
        FilmInfo r = new FilmInfo(555L,PARAMS1[0],(short)50,2L);
        doReturn(r).when(fs).updateFilmRating(PARAMS1[0],(short)50);
        doThrow(new IllegalStateException("not exist : "+PARAMS3[0])).when(fs).updateFilmRating(PARAMS3[0],(short)25);
        doThrow(new IllegalStateException("Illegal rating value. Must be[0..100]: ")).when(fs).updateFilmRating(PARAMS1[0],(short)-23);
        doThrow(new IllegalStateException("Illegal rating value. Must be[0..100]: ")).when(fs).updateFilmRating(PARAMS1[0],(short)-1);
        doThrow(new IllegalStateException("Illegal rating value. Must be[0..100]: ")).when(fs).updateFilmRating(PARAMS1[0 ],(short)101);

        mockMvc.perform(post("/v1/films/update/"+PARAMS1[0]+"?r=50").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(555L))
                .andExpect(jsonPath("$.name").value(PARAMS1[0]))
                .andExpect(jsonPath("$.rating").value(50))
                .andExpect(jsonPath("$.gid").value(2L));

        mockMvc.perform(post("/v1/films/update/"+PARAMS1[0]+"?r=101").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/v1/films/update/"+PARAMS1[0]+"?r=-23").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/v1/films/update/"+PARAMS3[0]+"?r=25").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/v1/films/update/"+PARAMS1[0]+"?r=").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/v1/films/update/"+PARAMS1[0]+"?r").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/v1/films/update/"+PARAMS1[0]).accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/v1/films/update/").accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().is(405));
    }

    @Test
    void testDelete() throws Exception {
        logger.info("testDelete");
        FilmInfo r = new FilmInfo(333L,"IT",(short)25,2L);
        doReturn(r).when(fs).deleteFilm(PARAMS1[0]);
        doThrow(new IllegalStateException("Film with name"+PARAMS3[0]+"was not found.")).when(fs).deleteFilm(PARAMS3[0]);


        mockMvc.perform(post("/v1/films/delete/"+PARAMS1[0]).accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id").value(333L))
                .andExpect(jsonPath("$.name").value(PARAMS1[0]))
                .andExpect(jsonPath("$.rating").value(25))
                .andExpect(jsonPath("$.gid").value(2L));

        mockMvc.perform(post("/v1/films/delete/"+PARAMS3[0]).accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());

    }
}
