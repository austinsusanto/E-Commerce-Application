package com.app.entites.review;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ReviewId.class)
public class Review {

    @Id
    private Long productId;

    @Id
    private Long userId;

    @Min(1)
    @Max(5)
    private short rating;

    private String comment;

    private LocalDateTime createdAt;
}
