package com.example.cognizant.Books.service;

import com.example.cognizant.Books.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "books.remote.url=https://invalid.example.com/nonexistent.json"
})
class BooksServiceTest {

    @Autowired
    private BooksService booksService;

    @Test
    void fallbackLoadsFromClasspath() {
        List<Book> books = booksService.loadBooks();
        assertFalse(books.isEmpty(), "Expected fallback books to load");
        assertNotNull(books.get(0).getIsbn());
    }

    @Test
    void searchReturnsMatches() {
        List<Book> books = booksService.search("javascript");
        assertFalse(books.isEmpty());
        assertTrue(books.stream().allMatch(b -> containsIgnoreCase(b.getTitle(), "javascript") || containsIgnoreCase(b.getDescription(), "javascript")));
    }

    private boolean containsIgnoreCase(String s, String sub) { return s != null && s.toLowerCase().contains(sub.toLowerCase()); }
}
