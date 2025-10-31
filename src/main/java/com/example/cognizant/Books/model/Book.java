package com.example.cognizant.Books.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Model class representing a Book entity.
 * Contains information about a book such as ISBN, title, author, etc.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {
    private String isbn;
    private String title;
    private String subtitle;
    private String author;
    private String published;
    private String publisher;
    private Integer pages;
    private String description;
    private String website;
}
