package at.ac.tuwien.sepm.assignment.individual.service;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseTreeDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import java.util.stream.Stream;

/**
 * Service for working with horses.
 */
public interface HorseService {
  /**
   * Lists all horses stored in the system.
   *
   * @return list of all stored horses
   */
  Stream<HorseListDto> allHorses();


  /**
   * Updates the horse with the ID given in {@code horse}
   * with the data given in {@code horse}
   * in the persistent data store.
   *
   * @param horse the horse to update
   * @return he updated horse
   * @throws NotFoundException if the horse with given ID does not exist in the persistent data store
   * @throws ValidationException if the update data given for the horse is in itself incorrect (description too long, no name, …)
   * @throws ConflictException if the update data given for the horse is in conflict the data currently in the system (owner does not exist, …)
   */
  HorseDetailDto update(HorseDetailDto horse) throws NotFoundException, ValidationException, ConflictException;


  /**
   * Get the horse with given ID, with more detail information.
   * This includes the owner of the horse, and its parents.
   * The parents of the parents are not included.
   *
   * @param id the ID of the horse to get
   * @return the horse with ID {@code id}
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   */
  HorseDetailDto getById(long id) throws NotFoundException;

  /**
   * Save the given horse in the database.
   *
   * @param toCreate The new horse to create
   * @return The final state of the horse as saved in the persistent data store
   * @throws ValidationException if the new data given for the horse is in itself incorrect (description too long, no name, …)
   */
  HorseDetailDto create(HorseCreateDto toCreate) throws ValidationException, ConflictException, NotFoundException;

  /**
   * Delete a single horse
   *
   * @param id The id of the horse to delete
   * @throws NotFoundException If no horse with the given id was found
   */
  void delete(long id) throws NotFoundException;

  /**
   * Search for horses based on search parameters which are connected using AND Operations
   *
   * @param searchParameters The parameters, that the returned horses must match
   * @return All horses which match the search parameters
   */
  Stream<HorseListDto> search(HorseSearchDto searchParameters);

  /**
   * Retrieve all ancestors for the given horse. The horse itself is part of generation 0,
   * the parents generation 1 and so on.
   *
   * @param id the id of the horse of which we want to receive the ancestor tree
   * @param generations the number of generations to be included in the tree (>0)
   * @return All ancestors of the given horse including the horse itself at the root, up to {@code generations} depth
   * @throws NotFoundException If no horse with the given id was found
   * @throws ValidationException If the given parameters fail validation checks for constraints
   */
  HorseTreeDto getAncestors(Long id, Integer generations) throws NotFoundException, ValidationException;
}
