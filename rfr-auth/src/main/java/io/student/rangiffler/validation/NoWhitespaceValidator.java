package io.student.rangiffler.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NoWhitespaceValidator implements ConstraintValidator<NoWhitespace, String> {
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value == null || value.chars().noneMatch(Character::isWhitespace);
  }
}
