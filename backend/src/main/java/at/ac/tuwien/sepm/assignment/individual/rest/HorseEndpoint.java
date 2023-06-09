package at.ac.tuwien.sepm.assignment.individual.rest;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseTreeDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.service.HorseService;
import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * The rest endpoint to manipulate and retrieve saved horses
 */
@RestController
@RequestMapping(path = HorseEndpoint.BASE_PATH)
public class HorseEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static final String BASE_PATH = "/horses";

  private final HorseService service;

  public HorseEndpoint(HorseService service) {
    this.service = service;
  }

  /**
   * Retrieve the ancestor tree of a given horse
   *
   * @param id the id of the horse to retrieve the generation tree from
   * @param generations the amount of generations to be contained in the tree. The horse itself is generation 0, their parents 1 and so on
   * @return A ancestor tree with the given horse at the root of it
   * @throws ValidationException If validation errors occur
   */
  @GetMapping("{id}/ancestors")
  public HorseTreeDto getAncestors(@PathVariable Long id, Integer generations) throws ValidationException {
    LOG.info("GET " + BASE_PATH + "/{}/ancestors", id);
    LOG.debug("request parameters: generations={}", generations);

    try {
      return service.getAncestors(id, generations);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to get ancestors of not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }


  /**
   * Retrieve horses based on a set of search criteria
   *
   * @param searchParameters The search criteria to narrow the amount of horses
   * @return All horses which mach the search criteria
   */
  @GetMapping
  public Stream<HorseListDto> searchHorses(HorseSearchDto searchParameters) {
    LOG.info("GET " + BASE_PATH);
    LOG.debug("request parameters: {}", searchParameters);

    return service.search(searchParameters);
  }

  /**
   * Retrieve a single horse by its id
   *
   * @param id The id of the horse
   * @return The corresponding horse with the same id, or an 404 if none were found
   */
  @GetMapping("{id}")
  public HorseDetailDto getById(@PathVariable long id) {
    LOG.info("GET " + BASE_PATH + "/{}", id);
    try {
      return service.getById(id);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to get details of not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }


  /**
   * Update the vlaues of a horse
   *
   * @param id The unique id of the horse to manipulate
   * @param toUpdate The new values
   * @return The resulting horse as saved in the persistent data store
   * @throws ValidationException If validation errors occur
   * @throws ConflictException If conflicts occur with dependent objects
   */
  @PutMapping("{id}")
  public HorseDetailDto update(@PathVariable long id, @RequestBody HorseDetailDto toUpdate) throws ValidationException, ConflictException {
    LOG.info("PUT " + BASE_PATH + "/{}", toUpdate);
    LOG.debug("Body of request:\n{}", toUpdate);
    try {
      return service.update(toUpdate.withId(id));
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to update not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  /**
   *  Create a new horse
   *
   * @param toCreate The values of the horse to create
   * @return The resulting horse as saved in the persistent data store
   * @throws ValidationException If validation errors occur
   */
  @PostMapping()
  @ResponseStatus(HttpStatus.CREATED)
  public HorseDetailDto create(@RequestBody HorseCreateDto toCreate) throws ValidationException, ConflictException, NotFoundException {
    LOG.info("POST " + BASE_PATH + "/");
    LOG.debug("Body of request:\n{}", toCreate);

    return service.create(toCreate);
  }

  /**
   * Delete a horse
   *
   * @param id the id of the horse to delete
   */
  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long id) {
    LOG.info("DELETE " + BASE_PATH + "/{}", id);

    try {
      service.delete(id);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to delete not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }


  private void logClientError(HttpStatus status, String message, Exception e) {
    LOG.warn("{} {}: {}: {}", status.value(), message, e.getClass().getSimpleName(), e.getMessage());
  }
}
