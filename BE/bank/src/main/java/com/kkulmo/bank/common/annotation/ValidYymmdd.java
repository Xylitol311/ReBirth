package com.kkulmo.bank.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.DateTimeException;
import java.time.Year;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = YymmddValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidYymmdd {
	String message() default "유효하지 않은 생년월일 형식입니다. YYMMDD 형식(예: 980101)이어야 합니다.";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
class YymmddValidator implements ConstraintValidator<ValidYymmdd, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.length() != 6) {
			return false;
		}

		// 숫자로만 이루어져 있는지 확인
		if (!value.matches("^\\d{6}$")) {
			return false;
		}

		try {
			int year = Integer.parseInt(value.substring(0, 2));
			int month = Integer.parseInt(value.substring(2, 4));
			int day = Integer.parseInt(value.substring(4, 6));

			// 월과 일의 기본 유효성 검사
			if (month < 1 || month > 12) {
				return false;
			}

			// 해당 월의 마지막 날짜보다 큰지 확인
			if (day < 1) {
				return false;
			}

			// 해당 월의 최대 일수 확인 (윤년 고려)
			int fullYear = year + (year >= 30 ? 1900 : 2000); // 년도 추론 (30 이상은 1900년대로 가정)
			boolean leapYear = Year.isLeap(fullYear);

			return switch (month) {
				case 2 -> leapYear ? day <= 29 : day <= 28;
				case 4, 6, 9, 11 -> day <= 30;
				default -> day <= 31; // 1, 3, 5, 7, 8, 10, 12월
			};

		} catch (NumberFormatException | DateTimeException e) {
			return false;
		}
	}
}