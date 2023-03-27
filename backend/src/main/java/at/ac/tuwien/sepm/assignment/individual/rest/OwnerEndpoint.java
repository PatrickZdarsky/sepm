package at.ac.tuwien.sepm.assignment.individual.rest;

import at.ac.tuwien.sepm.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.service.OwnerService;
import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(OwnerEndpoint.BASE_PATH)
public class OwnerEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static final String BASE_PATH = "/owners";

  private final OwnerService service;

  public OwnerEndpoint(OwnerService service) {
    this.service = service;
  }

  /**
   * Search for owners based on the given parameters
   *
   * @param searchParameters the search parameters to search for
   * @return all owners which math the given search parameters
   */
  @GetMapping
  public Stream<OwnerDto> search(OwnerSearchDto searchParameters) {
    LOG.info("GET " + BASE_PATH + " query parameters: {}", searchParameters);
    return service.search(searchParameters);
  }

  /**
   * Create a new owner
   *
   * @param owner the new owner to create
   * @return the created owner as it has been saved in the persistent data store
   * @throws ValidationException if validation errors occur
   * @throws ConflictException if the given owner has conflicts with existing entities
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OwnerDto create(@RequestBody OwnerCreateDto owner) throws ValidationException, ConflictException {
    LOG.info("POST " + BASE_PATH + "/");
    LOG.debug("Body of request:\n{}", owner);

    return service.create(owner);
  }

}
