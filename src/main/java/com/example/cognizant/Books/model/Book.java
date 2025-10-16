package com.example.cognizant.Books.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

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
