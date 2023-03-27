package at.ac.tuwien.sepm.assignment.individual.mapper;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailSimpleDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseTreeDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.FatalException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HorseMapper {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public HorseMapper() {
  }

  /**
   * Convert a list horse entity ancestors to a {@link HorseTreeDto}.
   * If not all ancestors are provided in the given list, the missing ones are ignored in the result.
   *
   * @param rootHorse the base of the ancestor tree
   * @param horses a list of ancestors of the {@code rootHorse}
   * @return the converted {@link HorseTreeDto}
   */
  public HorseTreeDto entityListToTreeDto(Horse rootHorse, List<Horse> horses) {
    Map<Long, Horse> horseMap = horses.stream().collect(Collectors.toMap(Horse::getId, horse -> horse));

    return fillAncestors(rootHorse, horseMap);
  }

  private HorseTreeDto fillAncestors(Horse horse, Map<Long, Horse> ancestors) {
    var father = Optional.ofNullable(horse.getFatherId())
            .map(ancestors::get)
            .map(hors -> fillAncestors(hors, ancestors)).orElse(null);
    var mother = Optional.ofNullable(horse.getMotherId())
            .map(ancestors::get)
            .map(hors -> fillAncestors(hors, ancestors)).orElse(null);

    return new HorseTreeDto(
            horse.getId(),
            horse.getName(),
            horse.getDescription(),
            horse.getDateOfBirth(),
            horse.getSex(),
            father,
            mother);
  }

  /**
   * Convert a horse entity object to a {@link HorseListDto}.
   * The given map of owners needs to contain the owner of {@code horse}.
   *
   * @param horse the horse to convert
   * @param owners a map of horse owners by their id, which needs to contain the owner referenced by {@code horse}
   * @return the converted {@link HorseListDto}
   */
  public HorseListDto entityToListDto(Horse horse, Map<Long, OwnerDto> owners) {
    LOG.trace("entityToDto({})", horse);
    if (horse == null) {
      return null;
    }

    return new HorseListDto(
        horse.getId(),
        horse.getName(),
        horse.getDescription(),
        horse.getDateOfBirth(),
        horse.getSex(),
        getOwner(horse, owners)
    );
  }

  /**
   * Convert a horse entity object to a {@link HorseDetailSimpleDto}.
   * The given map of owners needs to contain the owner of {@code horse}.
   *
   * @param horse the horse to convert
   * @param owners a map of horse owners by their id, which needs to contain the owner referenced by {@code horse}
   * @return the converted {@link HorseDetailSimpleDto}
   */
  public HorseDetailSimpleDto entityToDetailDto(
          Horse horse,
          Map<Long, OwnerDto> owners) {
    LOG.trace("entityToDto({})", horse);
    if (horse == null) {
      return null;
    }


    return new HorseDetailSimpleDto(
            horse.getId(),
            horse.getName(),
            horse.getDescription(),
            horse.getDateOfBirth(),
            horse.getSex(),
            getOwner(horse, owners)
    );
  }

  /**
   * Convert a horse entity object to a {@link HorseDetailDto}.
   * The given map of owners needs to contain the owner of {@code horse}.
   *
   * @param horse the horse to convert
   * @param father The father of the horse, or null
   * @param mother The mother of the horse, or null
   * @param owners a map of horse owners by their id, which needs to contain the owner referenced by {@code horse}
   * @return the converted {@link HorseDetailDto}
   */
  public HorseDetailDto entityToDetailDto(
          Horse horse, HorseDetailSimpleDto father, HorseDetailSimpleDto mother,
          Map<Long, OwnerDto> owners) {
    LOG.trace("entityToDto({})", horse);
    if (horse == null) {
      return null;
    }

    return new HorseDetailDto(
            horse.getId(),
            horse.getName(),
            horse.getDescription(),
            horse.getDateOfBirth(),
            horse.getSex(),
            getOwner(horse, owners),
            father,
            mother
    );
  }

  private OwnerDto getOwner(Horse horse, Map<Long, OwnerDto> owners) {
    OwnerDto owner = null;
    var ownerId = horse.getOwnerId();
    if (ownerId != null) {
      if (!owners.containsKey(ownerId)) {
        throw new FatalException("Given owner map does not contain owner of this Horse (%d)".formatted(horse.getId()));
      }
      owner = owners.get(ownerId);
    }
    return owner;
  }

}
