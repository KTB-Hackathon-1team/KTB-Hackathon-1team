package com.ktb.hackathon.dto.request;

import com.ktb.hackathon.entity.enums.ChildGender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ChildProfileCreateRequest(
	@NotBlank(message = "name은 필수입니다.")
	String name,
	@NotNull(message = "birthDate는 필수입니다.")
	LocalDate birthDate,
	@NotNull(message = "gender는 필수입니다.")
	ChildGender gender
) {
}
