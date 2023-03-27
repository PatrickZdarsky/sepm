package at.ac.tuwien.sepm.assignment.individual.dto;

import at.ac.tuwien.sepm.assignment.individual.type.Sex;

import java.time.LocalDate;

public record HorseTreeDto(
        Long id,
        String name,
        String description,
        LocalDate dateOfBirth,
        Sex sex,
        HorseTreeDto father,
        HorseTreeDto mother
) {

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
