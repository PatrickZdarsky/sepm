package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class OwnerValidator {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final Pattern MAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

  public void validateForCreate(OwnerCreateDto owner) throws ValidationException {
    LOG.trace("validateForCreate({})", owner);
    List<String> validationErrors = new ArrayList<>();

    validateName(validationErrors, owner.firstName(), "firstname");
    validateName(validationErrors, owner.lastName(), "lastname");
    validateEmail(validationErrors, owner.email());
    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of owner for create failed", validationErrors);
    }
  }

  private void validateName(List<String> validationErrors, String name, String nametype) {
    if (name == null) {
      validationErrors.add("Owner " + nametype + " is missing");
      return;
    }

    if (name.isBlank()) {
      validationErrors.add("Owner " + nametype + " is given but blank");
    }
    if (name.length() > 255) {
      validationErrors.add("Owner " + nametype + " too long: longer than 255 characters");
    }
  }

  private void validateEmail(List<String> validationErrors, String email) {
    if (email != null) {
      if (email.isBlank()) {
        validationErrors.add("Owner email is given but blank");
      }
      if (email.length() > 255) {
        validationErrors.add("Owner email too long: longer than 255 characters");
      }

      if (!MAIL_PATTERN.matcher(email).matches()) {
        validationErrors.add("Owner email is not in a valid format");
      }
    }
  }
}
