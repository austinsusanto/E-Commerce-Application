package com.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.app.entites.Discount;

@Repository
public interface DiscountRepo extends JpaRepository<Discount, String> {

    @Query("SELECT d FROM Discount d WHERE d.discountCode = ?1 AND d.isActive = true AND d.startTime <= CURRENT_TIMESTAMP AND d.endTime >= CURRENT_TIMESTAMP")
    Discount findByDiscountCode(String discountCode);

}