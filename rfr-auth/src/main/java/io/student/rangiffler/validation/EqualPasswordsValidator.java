package io.student.rangiffler.validation;

import io.student.rangiffler.model.RegistrationForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

public class EqualPasswordsValidator implements ConstraintValidator<EqualPasswords, RegistrationForm> {
  @Override
  public boolean isValid(RegistrationForm form, ConstraintValidatorContext context) {
    boolean isValid = Objects.equals(form.password(), form.passwordSubmit());
    if (!isValid) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
          .addPropertyNode("password")
          .addConstraintViolation();
    }
    return isValid;
  }
}
