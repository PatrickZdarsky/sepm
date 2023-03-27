package at.ac.tuwien.sepm.assignment.individual.service;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
public class HorseServiceTest {

  @Autowired
  HorseService horseService;

  @Test
  public void getAllReturnsAllStoredHorses() {
    List<HorseListDto> horses = horseService.allHorses()
        .toList();
    assertThat(horses.size()).isEqualTo(3);
    assertThat(horses)
        .map(HorseListDto::id, HorseListDto::sex)
        .contains(tuple(-1L, Sex.FEMALE))
        .contains(tuple(-2L, Sex.MALE))
        .contains(tuple(-3L, Sex.MALE));
  }

  @Test
  public void getAllMaleHorses() {
    List<HorseListDto> horses = horseService.search(
            new HorseSearchDto(null, null, null, Sex.MALE, null, null))
            .toList();
    assertThat(horses.size()).isEqualTo(2);
    assertThat(horses)
            .map(HorseListDto::id, HorseListDto::sex)
            .contains(tuple(-2L, Sex.MALE))
            .contains(tuple(-3L, Sex.MALE));
  }

  @Test
  @DirtiesContext
  public void createHorse() throws ValidationException, ConflictException, NotFoundException {
    var name = "Hans";
    var birthday = LocalDate.now().minusDays(1);
    var horse = new HorseCreateDto(name, null, birthday, Sex.FEMALE, null, null, null);

    var createdHorse = horseService.create(horse);

    //Check if the horse was created successfully
    assertThat(createdHorse.name()).isEqualTo(name);
    assertThat(createdHorse.dateOfBirth()).isEqualTo(birthday);
  }

  @Test
  public void updateNonexistantHors() {
    var toUpdate = new HorseDetailDto(0L, "Panwascher", "The real one!",
            LocalDate.now().minusDays(1), Sex.MALE, null, null, null);

    assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> horseService.update(toUpdate));
  }
}
