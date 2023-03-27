package at.ac.tuwien.sepm.assignment.individual.persistence;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import java.util.List;

/**
 * Data Access Object for horses.
 * Implements access functionality to the application's persistent data store regarding horses.
 */
public interface HorseDao {
  /**
   * Get all horses stored in the persistent data store.
   *
   * @return a list of all stored horses
   */
  List<Horse> getAll();


  /**
   * Update the horse with the ID given in {@code horse}
   *  with the data given in {@code horse}
   *  in the persistent data store.
   *
   * @param horse the horse to update
   * @return the updated horse
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse update(HorseDetailDto horse) throws NotFoundException;

  /**
   * Get a horse by its ID from the persistent data store.
   *
   * @param id the ID of the horse to get
   * @return the horse
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse getById(long id) throws NotFoundException;

  /**
   * Save a new horse in the persistent data store.
   *
   * @param horse The new horse to create
   * @return The final state of the horse as saved in the persistent data store
   */
  Horse create(HorseCreateDto horse);

  /**
   * Deletes a horse from the persistent data store.
   *
   * @param id the ID of the horse to delete.
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  void delete(long id) throws NotFoundException;

  /**
   * Search for horses based on search parameters which are connected using AND Operations
   *
   * @param searchFilter The parameters, that the returned horses must match
   * @return All horses which match the search parameters
   */
  public List<Horse> search(HorseSearchDto searchFilter);

  /**
   * Retrieve all ancestors of a horse up to the {@code generations} generation.
   *
   * @param rootId The root of the ancestors
   * @param generations The number of ancestors to retrieve
   * @return A List with all ancestors of the given horse up to the {@code generations} generation
   * @throws NotFoundException if the given {@code rootId} doesn't correlate to any horse in the persistent data store
   */
  List<Horse> getAncestors(long rootId, long generations) throws NotFoundException;
}
