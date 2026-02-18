package com.app.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "member_discounts")
@NoArgsConstructor
@AllArgsConstructor
public class MemberDiscount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long memberDiscountId;

	@NotBlank
	@Column(unique = true, nullable = false)
	private String membershipCode;

	private String memberName;

	private double discountPercentage;

	private boolean active = true;
}
