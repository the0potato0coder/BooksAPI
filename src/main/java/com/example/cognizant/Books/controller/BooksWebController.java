package com.example.cognizant.Books.controller;

import com.example.cognizant.Books.model.Book;
import com.example.cognizant.Books.service.BooksService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Web Controller for handling book-related web requests.
 * Provides web views for searching books and displaying results.
 */
@Controller
public class BooksWebController {

    private final BooksService booksService;

    /**
     * Constructor for BooksWebController.
     * @param booksService the service for book operations
     */
    public BooksWebController(BooksService booksService) {
        this.booksService = booksService;
    }

    /**
     * Displays the home page with search form.
     * @param model the Spring MVC model
     * @return the view name
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("books", List.of()); // Empty list initially
        return "books";
    }

    /**
     * Handles search form submission.
     * @param query the search query
     * @param model the Spring MVC model
     * @return the view name
     */
    @PostMapping("/search")
    public String search(@RequestParam(name = "query", required = false) String query, Model model) {
        List<Book> books = booksService.search(query);
        model.addAttribute("books", books);
        model.addAttribute("query", query);
        return "books";
    }

    /**
     * Reloads the book data and displays the updated count.
     * @param model the Spring MVC model
     * @return the view name
     */
    @GetMapping("/reload")
    public String reload(Model model) {
        int count = booksService.loadBooks().size();
        Map<String, Object> reloadInfo = Map.of("count", count, "lastLoaded", booksService.getLastLoaded().toString());
        model.addAttribute("reloadInfo", reloadInfo);
        model.addAttribute("books", List.of()); // Empty list
        return "books";
    }

    /**
     * Handles advanced search form submission with keywords and match mode.
     * @param keywords the comma-separated keywords
     * @param matchMode the match mode (ANY or ALL)
     * @param model the Spring MVC model
     * @return the view name
     */
    @PostMapping("/search/advanced")
    public String searchAdvanced(@RequestParam(name = "keywords") String keywords,
                                @RequestParam(name = "matchMode", defaultValue = "ANY") String matchMode,
                                Model model) {
        // Split keywords by comma and trim whitespace
        List<String> keywordList = List.of(keywords.split(","))
                .stream()
                .map(String::trim)
                .filter(k -> !k.isEmpty())
                .toList();

        boolean matchAll = "ALL".equalsIgnoreCase(matchMode);
        List<Book> books = booksService.searchKeywords(keywordList, matchAll);

        model.addAttribute("books", books);
        model.addAttribute("keywords", keywords);
        model.addAttribute("matchMode", matchMode);
        model.addAttribute("searchType", "advanced");
        return "books";
    }
}
