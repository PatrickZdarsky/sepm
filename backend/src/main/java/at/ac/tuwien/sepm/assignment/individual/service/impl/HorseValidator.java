package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailSimpleDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HorseValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Validate parameters for horse ancestor retrieval
   *
   * @param id the root horse
   * @param generations the max generation hops, has to be bigger than 0
   * @throws ValidationException if a parameter is failing validation checks
   */
  public void validateForAncestorRetrieval(Long id, Integer generations) throws ValidationException {
    LOG.trace("validateForAncestorRetrieval({}, {})", id, generations);

    List<String> validationErrors = new ArrayList<>();
    validateId(validationErrors, id);

    if (generations == null) {
      validationErrors.add("Ancestor generations are missing");
    } else if (generations < 0) {
      validationErrors.add("Ancestor generations must be positive");
    }

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of ancestor retrieval failed", validationErrors);
    }
  }

  /**
   * Validate parameters to create new horse
   *
   * @param horse The horse to be created
   * @param father existing horse which should be set as father
   * @param mother existing horse which should be set as mother
   * @throws ValidationException if a parameter is failing validation checks
   * @throws ConflictException if the given constellation of parents is illegal
   */
  public void validateForCreate(HorseCreateDto horse, HorseDetailSimpleDto father, HorseDetailSimpleDto mother) throws ValidationException, ConflictException {
    LOG.trace("validateForCreate({})", horse);
    List<String> validationErrors = new ArrayList<>();

    validateSex(validationErrors, horse.sex());
    validateBirthDay(validationErrors, horse.dateOfBirth());
    validateName(validationErrors, horse.name());
    validateDescription(validationErrors, horse.description());

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }

    List<String> conflictErrors = new ArrayList<>();
    validateParents(conflictErrors, null, horse.dateOfBirth(), father, mother);

    if (!conflictErrors.isEmpty()) {
      throw new ConflictException("Data of horse for update has conflicts", conflictErrors);
    }
  }

  /**
   * Validate parameters to update an existing horse
   *
   * @param horse existing horse with new values to be updated
   * @param father existing horse which should be set as father
   * @param mother existing horse which should be set as mother
   * @throws ValidationException if a parameter is failing validation checks
   * @throws ConflictException if the given constellation of parents is illegal
   */
  public void validateForUpdate(HorseDetailDto horse, HorseDetailSimpleDto father, HorseDetailSimpleDto mother, boolean isParent, Sex oldSex) throws ValidationException, ConflictException {
    LOG.trace("validateForUpdate({})", horse);
    List<String> validationErrors = new ArrayList<>();

    validateId(validationErrors, horse.id());
    validateSex(validationErrors, horse.sex());
    validateBirthDay(validationErrors, horse.dateOfBirth());
    validateName(validationErrors, horse.name());
    validateDescription(validationErrors, horse.description());

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }

    List<String> conflictErrors = new ArrayList<>();
    validateParents(conflictErrors, horse.id(), horse.dateOfBirth(), father, mother);

    if (isParent && oldSex != horse.sex()) {
      conflictErrors.add("Cannot change sex of horse which has children");
    }

    if (!conflictErrors.isEmpty()) {
      throw new ConflictException("Data of horse for update has conflicts", conflictErrors);
    }
  }

  private void validateParents(List<String> validationErrors, Long horseId, LocalDate horseBirthDay, HorseDetailSimpleDto father, HorseDetailSimpleDto mother) {
    if (father != null) {
      if (horseId != null && horseId == father.id()) {
        validationErrors.add("The father of the horse cannot be the horse itself");
      }
      if (father.sex() != Sex.MALE) {
        validationErrors.add("The father has to be a male");
      }
      if (father.dateOfBirth().isAfter(horseBirthDay)) {
        validationErrors.add("The father cannot be born after the child");
      }
    }

    if (mother != null) {
      if (horseId != null && horseId == mother.id()) {
        validationErrors.add("The mother of the horse cannot be the horse itself");
      }
      if (mother.sex() != Sex.FEMALE) {
        validationErrors.add("The mother has to be a female");
      }
      if (mother.dateOfBirth().isAfter(horseBirthDay)) {
        validationErrors.add("The mother cannot be born after the child");
      }
    }
  }

  private static void validateDescription(List<String> validationErrors, String description) {
    if (description != null) {
      if (description.isBlank()) {
        validationErrors.add("Horse description is given but blank");
      }
      if (description.length() > 4095) {
        validationErrors.add("Horse description too long: longer than 4095 characters");
      }
    }
  }

  private void validateName(List<String> validationErrors, String name) {
    if (name != null) {
      if (name.isBlank()) {
        validationErrors.add("Horse name is given but blank");
      }
      if (name.length() > 255) {
        validationErrors.add("Horse name too long: longer than 255 characters");
      }
    } else {
      validationErrors.add("Horse name is not set");
    }
  }

  private void validateId(List<String> validationErrors, Long id) {
    if (id == null) {
      validationErrors.add("No ID given");
    }
  }

  private void validateSex(List<String> validationErrors, Sex sex) {
    if (sex == null) {
      validationErrors.add("No sex given");
    }
  }

  private void validateBirthDay(List<String> validationErrors, LocalDate birthday) {
    if (birthday == null) {
      validationErrors.add("Horse date of birth is missing");
    } else if (birthday.isAfter(LocalDate.now())) {
      validationErrors.add("Horse birth is in the future");
    }
  }
}
