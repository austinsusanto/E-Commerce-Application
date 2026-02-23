package com.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.app.payloads.ReviewDTO;
import com.app.services.ReviewService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    public ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(@RequestBody Long productId, @RequestBody Long userId,
            @RequestBody short rating, @RequestBody String comment) {
        ReviewDTO reviewDTO = reviewService.createReview(
                productId, userId, rating, comment);
        return new ResponseEntity<>(reviewDTO, HttpStatus.CREATED);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDTO>> getRecentReviews(
            @PathVariable Long productId,
            @RequestParam(required = false) Integer limit) {

        List<ReviewDTO> reviews = reviewService.getRecentReviewsByProduct(productId, limit);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

}