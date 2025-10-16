package com.example.cognizant.Books.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "books.remote.url=https://invalid.example.com/nonexistent.json"
})
class BooksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getSearchEndpointWorks() throws Exception {
        mockMvc.perform(get("/api/books").param("query", "javascript"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").exists());
    }

    @Test
    void postSearchEndpointAnyMode() throws Exception {
        String body = "{\"keywords\":[\"modern\",\"press\"],\"matchMode\":\"ANY\"}";
        mockMvc.perform(post("/api/books/search").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isbn").exists());
    }
}
