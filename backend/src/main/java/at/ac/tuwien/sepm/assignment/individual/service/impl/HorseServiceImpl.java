package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.*;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.mapper.HorseMapper;
import at.ac.tuwien.sepm.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepm.assignment.individual.service.HorseService;
import at.ac.tuwien.sepm.assignment.individual.service.OwnerService;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The implementation of the horse service to manage horses
 */
@Service
public class HorseServiceImpl implements HorseService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final HorseDao dao;
  private final HorseMapper mapper;
  private final HorseValidator validator;
  private final OwnerService ownerService;

  /**
   * Default DI constructor.
   *
   * @param dao The DAO to manipulate saved horses
   * @param mapper The mapper to convert between DTO's and entities
   * @param validator The validator to validate entities
   * @param ownerService The owner service to look up owner references from horses
   */
  public HorseServiceImpl(HorseDao dao, HorseMapper mapper, HorseValidator validator, OwnerService ownerService) {
    this.dao = dao;
    this.mapper = mapper;
    this.validator = validator;
    this.ownerService = ownerService;
  }

  @Override
  public Stream<HorseListDto> allHorses() {
    LOG.trace("allHorses()");
    var horses = dao.getAll();
    var ownerIds = horses.stream()
        .map(Horse::getOwnerId)
        .filter(Objects::nonNull)
        .collect(Collectors.toUnmodifiableSet());
    Map<Long, OwnerDto> ownerMap;
    try {
      ownerMap = ownerService.getAllById(ownerIds);
    } catch (NotFoundException e) {
      throw new FatalException("Horse, that is already persisted, refers to non-existing owner", e);
    }
    return horses.stream()
        .map(horse -> mapper.entityToListDto(horse, ownerMap));
  }


  @Override
  public HorseDetailDto update(HorseDetailDto horse) throws NotFoundException, ValidationException, ConflictException {
    LOG.trace("update({})", horse);

    var simpleHorse = horse.withoutParents();
    var father = horse.father() == null ? null : dao.getById(horse.father().id());
    var mother = horse.mother() == null ? null : dao.getById(horse.mother().id());

    var owners = ownerMap(horse.ownerId(), father == null ? null : father.getOwnerId(), mother == null ? null : mother.getOwnerId());
    var fatherDto = father == null ? null : mapper.entityToDetailDto(father, owners);
    var motherDto = mother == null ? null : mapper.entityToDetailDto(mother, owners);

    validator.validateForUpdate(horse, fatherDto, motherDto, dao.isParent(horse.id()), dao.getById(horse.id()).getSex());


    var updatedHorse = dao.update(horse);



    return mapper.entityToDetailDto(
        updatedHorse, fatherDto, motherDto,
        owners);
  }


  @Override
  public HorseDetailDto getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);

    var horse = dao.getById(id);
    var father = horse.getFatherId() == null ? null : dao.getById(horse.getFatherId());
    var mother = horse.getMotherId() == null ? null : dao.getById(horse.getMotherId());

    var owners = ownerMap(horse.getOwnerId(), father == null ? null : father.getOwnerId(), mother == null ? null : mother.getOwnerId());
    return mapper.entityToDetailDto(
            horse,
            mapper.entityToDetailDto(father, owners),
            mapper.entityToDetailDto(mother, owners),
            owners);
  }

  @Override
  public HorseDetailDto create(HorseCreateDto toCreate) throws ValidationException, ConflictException, NotFoundException {
    LOG.trace("create({})", toCreate);

    var father = toCreate.fatherId() == null ? null : dao.getById(toCreate.fatherId());
    var mother = toCreate.motherId() == null ? null : dao.getById(toCreate.motherId());
    var owners = ownerMap(toCreate.ownerId(), father == null ? null : father.getOwnerId(), mother == null ? null : mother.getOwnerId());
    var fatherDto = father == null ? null : mapper.entityToDetailDto(father, ownerMapForSingleId(father.getOwnerId()));
    var motherDto = mother == null ? null : mapper.entityToDetailDto(mother, ownerMapForSingleId(mother.getOwnerId()));

    validator.validateForCreate(toCreate, fatherDto, motherDto);
    Horse horse = dao.create(toCreate);

    return mapper.entityToDetailDto(
            horse,
            mapper.entityToDetailDto(father, owners),
            mapper.entityToDetailDto(mother, owners),
            owners);
  }

  @Override
  public void delete(long id) throws NotFoundException {
    LOG.trace("delete({})", id);

    dao.delete(id);
  }

  @Override
  public Stream<HorseListDto> search(HorseSearchDto searchParameters) {
    LOG.trace("search({})", searchParameters);

    return dao.search(searchParameters).stream().map(horse -> mapper.entityToListDto(horse, ownerMapForSingleId(horse.getOwnerId())));
  }

  @Override
  public HorseTreeDto getAncestors(Long id, Integer generations) throws NotFoundException, ValidationException {
    LOG.trace("getAncestors({}, {})", id, generations);

    validator.validateForAncestorRetrieval(id, generations);

    var horses = dao.getAncestors(id, generations);
    var root = horses.stream().filter(horse -> horse.getId() == id).findAny()
            .orElseThrow(() -> new FatalException("Horse ancestors are missing horse itself"));

    return mapper.entityListToTreeDto(root, horses);
  }


  private Map<Long, OwnerDto> ownerMapForSingleId(Long ownerId) {
    try {
      return ownerId == null
              ? null
              : Collections.singletonMap(ownerId, ownerService.getById(ownerId));
    } catch (NotFoundException e) {
      throw new FatalException("Owner %d referenced by horse not found".formatted(ownerId));
    }
  }

  private Map<Long, OwnerDto> ownerMap(Long... ownerIds) {
    if (ownerIds == null) {
      return null;
    }

    var map = new HashMap<Long, OwnerDto>();
    for (var ownerId : ownerIds) {
      if (ownerId != null) {
        try {
          map.put(ownerId, ownerService.getById(ownerId));
        } catch (NotFoundException e) {
          throw new FatalException("Owner %d referenced by horse not found".formatted(ownerId));
        }
      }
    }

    return map;
  }
}
