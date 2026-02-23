package com.app.entites;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_discounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreDiscount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long discountId;
	
	private String discountName;
	
	private Double discountValue;
	
	private LocalDateTime startTime;
	
	private LocalDateTime endTime;
	
	private Boolean isActive;
}
