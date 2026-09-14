package com.movieplatform.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class CategoryHasMovieId implements Serializable {
    private static final long serialVersionUID = 5691480510665140747L;
    @NotNull
    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @NotNull
    @Column(name = "movie_id", nullable = false)
    private Integer movieId;


}