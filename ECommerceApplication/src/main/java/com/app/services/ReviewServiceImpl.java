package com.app.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.app.entites.review.Review;
import com.app.entites.review.ReviewId;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.ReviewDTO;
import com.app.repositories.ReviewRepo;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    public ReviewRepo reviewRepo;

    @Autowired
    public ModelMapper modelMapper;

    private static final Integer DEFAULT_RECENT_LIMIT = 10;

    @Override
    public ReviewDTO createReview(Long productId, Long userId, Short rating, String comment) {

        // Validate rating range
        if (rating == null || rating < 1 || rating > 5) {
            throw new APIException("Rating must be between 1 and 5");
        }

        ReviewId compositeKey = new ReviewId(productId, userId);

        // Check if review already exists
        Review existingReview = reviewRepo.findById(compositeKey).orElse(null);

        Review review;
        if (existingReview != null) {
            // Update existing review
            existingReview.setRating(rating);
            existingReview.setComment(comment);
            // Keep original createdAt - business decision
            review = reviewRepo.save(existingReview);
        } else {
            // Create new review
            review = new Review();
            review.setProductId(productId);
            review.setUserId(userId);
            review.setRating(rating);
            review.setComment(comment);
            review.setCreatedAt(LocalDateTime.now());
            review = reviewRepo.save(review);
        }

        return modelMapper.map(review, ReviewDTO.class);
    }

    @Override
    public List<ReviewDTO> getRecentReviewsByProduct(Long productId, Integer limit) {

        // Set default limit if null or invalid
        if (limit == null || limit <= 0) {
            limit = DEFAULT_RECENT_LIMIT;
        }

        // Cap maximum limit to prevent abuse
        if (limit > 100) {
            limit = 100;
        }

        Sort sortByAndOrder = Sort.by("createdAt").descending();
        Pageable pageDetails = PageRequest.of(0, limit, sortByAndOrder);

        List<Review> reviews = reviewRepo.findByProductIdOrderByCreatedAtDesc(productId, pageDetails);

        if (reviews.isEmpty()) {
            throw new APIException("No reviews found for product with id: " + productId);
        }

        return reviews.stream()
                .map(review -> modelMapper.map(review, ReviewDTO.class))
                .collect(Collectors.toList());

    }

}