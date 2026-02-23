package com.app.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.entites.MemberDiscount;

@Repository
public interface MemberDiscountRepo extends JpaRepository<MemberDiscount, Long> {

	Optional<MemberDiscount> findByMembershipCode(String membershipCode);
}
