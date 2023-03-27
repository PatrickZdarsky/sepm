package at.ac.tuwien.sepm.assignment.individual.persistence;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;

import java.time.LocalDate;
import java.util.List;

import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
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
public class HorseDaoTest {

  @Autowired
  HorseDao horseDao;

  @Test
  public void getAllReturnsAllStoredHorses() {
    List<Horse> horses = horseDao.getAll();
    assertThat(horses.size()).isEqualTo(3);
    assertThat(horses)
        .extracting(Horse::getId, Horse::getName)
        .contains(tuple(-1L, "Wendy"))
        .contains(tuple(-2L, "Tom"))
        .contains(tuple(-3L, "Paul"));
  }

  @Test
  @DirtiesContext
  public void updateHors() throws NotFoundException {
    var newValues = new HorseDetailDto(-2L, "Tom", "The legend!",
            LocalDate.of(2012, 12, 13), Sex.MALE, null, null, null);
    var updated = horseDao.update(newValues);

    assertThat(updated.getId()).isEqualTo(newValues.id());
    assertThat(updated.getName()).isEqualTo(newValues.name());
    assertThat(updated.getDescription()).isEqualTo(newValues.description());
    assertThat(updated.getDateOfBirth()).isEqualTo(newValues.dateOfBirth());
  }

  @Test
  public void getNonexistantHorse() {
    assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> horseDao.getById(0));
  }

  @Test
  public void getHorsById() throws NotFoundException {
    var horse = horseDao.getById(-1);

    assertThat(horse).isNotNull();
    assertThat(horse.getName()).isEqualTo("Wendy");
  }
}
