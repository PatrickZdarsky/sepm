package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HorseValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void validateForCreate(HorseCreateDto horse) throws ValidationException {
    LOG.trace("validateForCreate({})", horse);

    List<String> validationErrors = new ArrayList<>();

    if (horse.description() != null) {
      if (horse.description().isBlank()) {
        validationErrors.add("Horse description is given but blank");
      }
      if (horse.description().length() > 4095) {
        validationErrors.add("Horse description too long: longer than 4095 characters");
      }
    }

    if (horse.dateOfBirth() == null) {
      validationErrors.add("Date of birth is missing");
    } else if (horse.dateOfBirth().isAfter(LocalDate.now())) {
      validationErrors.add("Date of birth is in the future");
    }

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }
  }

  public void validateForUpdate(HorseDetailDto horse) throws ValidationException, ConflictException {
    LOG.trace("validateForUpdate({})", horse);
    List<String> validationErrors = new ArrayList<>();

    if (horse.id() == null) {
      validationErrors.add("No ID given");
    }

    if (horse.description() != null) {
      if (horse.description().isBlank()) {
        validationErrors.add("Horse description is given but blank");
      }
      if (horse.description().length() > 4095) {
        validationErrors.add("Horse description too long: longer than 4095 characters");
      }
    }

    // TODO this is not complete…

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }
  }

}
