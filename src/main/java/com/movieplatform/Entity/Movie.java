package com.movieplatform.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "movie")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 45)
    @Column(name = "name", length = 45)
    private String name;

    @Size(max = 45)
    @Column(name = "language", length = 45)
    private String language;

    @Size(max = 45)
    @Column(name = "country", length = 45)
    private String country;

    @Column(name = "duration")
    private Instant duration;

    @Size(max = 45)
    @Column(name = "shortdescription", length = 45)
    private String shortdescription;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @Size(max = 255)
    @Column(name = "image")
    private String image;

    @Size(max = 255)
    @Column(name = "link")
    private String link;

    @Size(max = 255)
    @Column(name = "trailerlink")
    private String trailerlink;

    @Column(name = "imdb")
    private Double imdb;

    @Column(name = "tomato")
    private Double tomato;

    @Column(name = "price")
    private Double price;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;


}