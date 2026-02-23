package com.app.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.app.entites.StoreDiscount;

@Repository
public interface StoreDiscountRepo extends JpaRepository<StoreDiscount, Long> {
	
	@Query("SELECT sd FROM StoreDiscount sd WHERE sd.isActive = true AND sd.startTime <= ?1 AND sd.endTime >= ?1")
	List<StoreDiscount> findActiveDiscountsByTime(LocalDateTime currentTime);
	
	@Query("SELECT sd FROM StoreDiscount sd WHERE sd.isActive = true AND sd.startTime <= CURRENT_TIMESTAMP AND sd.endTime >= CURRENT_TIMESTAMP")
	List<StoreDiscount> findCurrentActiveDiscounts();
}
