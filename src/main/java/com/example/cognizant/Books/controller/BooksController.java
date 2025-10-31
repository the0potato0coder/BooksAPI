package com.example.cognizant.Books.controller;

import com.example.cognizant.Books.model.Book;
import com.example.cognizant.Books.service.BooksService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for handling book-related API requests.
 * Provides endpoints for searching books, reloading data, and health checks.
 */
@RestController
@RequestMapping(path = "/api/books", produces = MediaType.APPLICATION_JSON_VALUE)
public class BooksController {

    private final BooksService booksService;

    /**
     * Constructor for BooksController.
     * @param booksService the service for book operations
     */
    public BooksController(BooksService booksService) {
        this.booksService = booksService;
    }

    /**
     * Searches for books using a query parameter.
     * @param query the search query (optional)
     * @return list of matching books
     */
    @GetMapping
    public List<Book> search(@RequestParam(name = "query", required = false) String query) {
        return booksService.search(query);
    }

    /**
     * Record for search request payload.
     */
    public record SearchRequest(List<String> keywords, String matchMode) {}

    /**
     * Searches for books using keywords in the request body.
     * @param request the search request with keywords and match mode
     * @return list of matching books
     */
    @PostMapping(path = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<Book> searchPost(@RequestBody SearchRequest request) {
        boolean matchAll = request.matchMode() != null && request.matchMode().equalsIgnoreCase("ALL");
        return booksService.searchKeywords(request.keywords(), matchAll);
    }

    /**
     * Reloads the book data and returns the count and last loaded time.
     * @return map with count and lastLoaded
     */
    @GetMapping("/reload")
    public Map<String,Object> reload() {
        int count = booksService.loadBooks().size();
        return Map.of("count", count, "lastLoaded", booksService.getLastLoaded().toString());
    }


    /**
     * Health check endpoint.
     * @return map with status
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
