package at.ac.tuwien.sepm.assignment.individual.dto;

import at.ac.tuwien.sepm.assignment.individual.type.Sex;

import java.time.LocalDate;

/**
 * DTO to create a new horse
 *
 * @param name The name of the horse
 * @param description The description of the horse
 * @param dateOfBirth The DateOfBirth of the horse
 * @param sex The sex of the horse
 * @param owner The horse's owner
 */
public record HorseCreateDto(
    String name,
    String description,
    LocalDate dateOfBirth,
    Sex sex,
    OwnerDto owner,
    HorseDetailDto father,
    HorseDetailDto mother
) {
  public Long ownerId() {
    return owner == null
            ? null
            : owner.id();
  }

  public Long fatherId() {
    return father == null
            ? null
            : father.id();
  }

  public Long motherId() {
    return mother == null
            ? null
            : mother.id();
  }
}
