package com.example.cognizant.Books.controller;

import com.example.cognizant.Books.model.Book;
import com.example.cognizant.Books.service.BooksService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/books", produces = MediaType.APPLICATION_JSON_VALUE)
public class BooksController {

    private final BooksService booksService;

    public BooksController(BooksService booksService) {
        this.booksService = booksService;
    }

    @GetMapping
    public List<Book> search(@RequestParam(name = "query", required = false) String query) {
        return booksService.search(query);
    }

    public record SearchRequest(List<String> keywords, String matchMode) {}

    @PostMapping(path = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<Book> searchPost(@RequestBody SearchRequest request) {
        boolean matchAll = request.matchMode() != null && request.matchMode().equalsIgnoreCase("ALL");
        return booksService.searchKeywords(request.keywords(), matchAll);
    }

    @GetMapping("/reload")
    public Map<String,Object> reload() {
        int count = booksService.loadBooks().size();
        return Map.of("count", count, "lastLoaded", booksService.getLastLoaded().toString());
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
