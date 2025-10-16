package com.example.cognizant.Books.service;

import com.example.cognizant.Books.model.Book;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BooksService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    @SuppressWarnings("unused")
    private final String remoteUrl;
    private volatile List<Book> cache = Collections.emptyList();
    @Getter
    private volatile Instant lastLoaded = Instant.EPOCH;

    public BooksService(ObjectMapper objectMapper,
                        @Value("${books.remote.url}") String remoteUrl) {
        this.objectMapper = objectMapper;
        this.remoteUrl = remoteUrl;
        this.restClient = RestClient.builder().baseUrl(remoteUrl).build();
    }

    public List<Book> loadBooks() {
        if (!cache.isEmpty()) {
            return cache;
        }
        List<Book> books = fetchRemote();
        if (books.isEmpty()) {
            books = loadFromClasspath();
        }
        cache = books;
        lastLoaded = Instant.now();
        return cache;
    }

    private List<Book> fetchRemote() {
        try {
            String json = restClient.get().accept(MediaType.APPLICATION_JSON).retrieve().body(String.class);
            return parse(json);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<Book> loadFromClasspath() {
        try (InputStream is = new ClassPathResource("books.json").getInputStream()) {
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return parse(json);
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private List<Book> parse(String json) {
        if (!StringUtils.hasText(json)) return Collections.emptyList();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode booksNode = root.path("books");
            List<Book> result = new ArrayList<>();
            if (booksNode.isArray()) {
                for (JsonNode n : booksNode) {
                    Book b = objectMapper.convertValue(n, Book.class);
                    result.add(b);
                }
            }
            return result;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Book> search(String query) {
        if (!StringUtils.hasText(query)) return loadBooks();
        String q = query.toLowerCase(Locale.ROOT);
        return loadBooks().stream()
                .filter(b -> contains(b, q))
                .collect(Collectors.toList());
    }

    public List<Book> searchKeywords(List<String> keywords, boolean matchAll) {
        List<String> lower = keywords == null ? List.of() : keywords.stream().filter(StringUtils::hasText)
                .map(s -> s.toLowerCase(Locale.ROOT)).toList();
        if (lower.isEmpty()) return loadBooks();
        return loadBooks().stream().filter(b -> matchAll ? lower.stream().allMatch(k -> contains(b, k)) : lower.stream().anyMatch(k -> contains(b, k)))
                .collect(Collectors.toList());
    }

    private boolean contains(Book b, String q) {
        return streamFields(b).anyMatch(f -> f.contains(q));
    }

    private java.util.stream.Stream<String> streamFields(Book b) {
        return java.util.stream.Stream.of(
                b.getTitle(), b.getSubtitle(), b.getAuthor(), b.getDescription(), b.getPublisher(), b.getIsbn()
        ).filter(Objects::nonNull).map(s -> s.toLowerCase(Locale.ROOT));
    }

}
