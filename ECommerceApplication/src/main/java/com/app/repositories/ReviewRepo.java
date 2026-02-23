package com.app.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import com.app.entites.review.Review;
import com.app.entites.review.ReviewId;

@Repository
public interface ReviewRepo extends JpaRepository<Review, ReviewId> {

    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

}