package com.app.entites.review;

import java.io.Serializable;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ReviewId implements Serializable {
    private Long productId;

    private Long userId;
}
