package at.ac.tuwien.sepm.assignment.individual.dto;

import at.ac.tuwien.sepm.assignment.individual.type.Sex;

import java.time.LocalDate;

public record HorseDetailSimpleDto(
        Long id,
        String name,
        String description,
        LocalDate dateOfBirth,
        Sex sex,
        OwnerDto owner
) {
  public HorseDetailSimpleDto withId(long newId) {
    return new HorseDetailSimpleDto(
            newId,
            name,
            description,
            dateOfBirth,
            sex,
            owner);
  }

  public Long ownerId() {
    return owner == null
            ? null
            : owner.id();
  }
}