package com.app.repositories;

import com.app.entites.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepo extends JpaRepository<Wishlist, Long> {
    
    List<Wishlist> findByUserUserIdOrderByAddedAtDesc(Long userId);
    
    Optional<Wishlist> findByUserUserIdAndProductProductId(Long userId, Long productId);
    
    void deleteByUserUserIdAndProductProductId(Long userId, Long productId);
    
    boolean existsByUserUserIdAndProductProductId(Long userId, Long productId);
    
    List<Wishlist> findByProductProductId(Long productId);
    
    void deleteByUserUserId(Long userId);
}
