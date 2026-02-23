package com.app.payloads;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {

    private Long productId;
    private Long userId;
    private Short rating;
    private String comment;
    private LocalDateTime createdAt;
}