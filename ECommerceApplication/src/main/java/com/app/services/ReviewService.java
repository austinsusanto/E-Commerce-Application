package com.app.services;

import java.util.List;

import com.app.payloads.ReviewDTO;

public interface ReviewService {

    ReviewDTO createReview(Long productId, Long userId, Short rating, String comment);

    List<ReviewDTO> getRecentReviewsByProduct(Long productId, Integer limit);

}