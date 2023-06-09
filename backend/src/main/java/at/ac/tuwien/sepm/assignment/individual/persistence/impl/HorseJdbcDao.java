package at.ac.tuwien.sepm.assignment.individual.persistence.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import java.lang.invoke.MethodHandles;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

/**
 * The data access object for horses
 */
@Repository
public class HorseJdbcDao implements HorseDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String TABLE_NAME = "horse";
  private static final String SQL_SELECT_ALL = "SELECT * FROM " + TABLE_NAME;
  private static final String SQL_SELECT_BY_ID = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
  private static final String SQL_UPDATE = "UPDATE " + TABLE_NAME
      + " SET name = ?"
      + "  , description = ?"
      + "  , date_of_birth = ?"
      + "  , sex = ?"
      + "  , owner_id = ?"
      + "  , father_id = ?"
      + "  , mother_id = ?"
      + " WHERE id = ?";
  private static final String SQL_CREATE = "INSERT INTO " + TABLE_NAME
          + " (name, description, date_of_birth, sex, owner_id, father_id, mother_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
  private static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME
          + " WHERE id=?";
  private static final String SQL_SEARCH = "SELECT * FROM " + TABLE_NAME + " WHERE 1=1";
  private static final String SQL_GET_ANCESTORS = "SELECT *  FROM horse "
          + "WHERE id IN (WITH ancestors (id, name, mother_id, father_id, generation) "
          + "AS (SELECT id, name, father_id, mother_id, 0 AS generation FROM " + TABLE_NAME
          + " WHERE id = ? UNION ALL SELECT h.id, h.name, h.father_id, h.mother_id, a.generation + 1"
          + " FROM ancestors a JOIN horse h ON h.id = a.father_id OR h.id = a.mother_id WHERE a.generation < ?)"
          + " SELECT DISTINCT id FROM ancestors);";
  private static final String SQL_GET_DIRECT_CHILDREN = "SELECT * FROM " + TABLE_NAME + " WHERE father_id = ? OR mother_id = ?";
  private final JdbcTemplate jdbcTemplate;

  public HorseJdbcDao(
      JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public List<Horse> getAll() {
    LOG.trace("getAll()");
    return jdbcTemplate.query(SQL_SELECT_ALL, this::mapRow);
  }

  @Override
  public Horse getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    List<Horse> horses;
    horses = jdbcTemplate.query(SQL_SELECT_BY_ID, this::mapRow, id);

    if (horses.isEmpty()) {
      throw new NotFoundException("No horse with ID %d found".formatted(id));
    }
    if (horses.size() > 1) {
      // This should never happen!!
      throw new FatalException("Too many horses with ID %d found".formatted(id));
    }

    return horses.get(0);
  }

  @Override
  public Horse create(HorseCreateDto horse) {
    LOG.trace("create({})", horse);

    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(con -> {
      PreparedStatement stmt = con.prepareStatement(SQL_CREATE, Statement.RETURN_GENERATED_KEYS);
      stmt.setString(1, horse.name());
      stmt.setString(2, horse.description());
      stmt.setDate(3, Date.valueOf(horse.dateOfBirth()));
      stmt.setString(4, horse.sex().toString());
      stmt.setObject(5, horse.ownerId());
      stmt.setObject(6, horse.fatherId());
      stmt.setObject(7, horse.motherId());
      return stmt;
    }, keyHolder);

    Number key = keyHolder.getKey();
    if (key == null) {
      // This should never happen. If it does, something is wrong with the DB or the way the prepared statement is set up.
      throw new FatalException("Could not extract key for newly created horse. There is probably a programming error…");
    }

    return new Horse()
            .setId(key.longValue())
            .setName(horse.name())
            .setDescription(horse.description())
            .setSex(horse.sex())
            .setDateOfBirth(horse.dateOfBirth())
            .setOwnerId(horse.ownerId())
            .setFatherId(horse.fatherId())
            .setMotherId(horse.motherId());
  }

  @Override
  public void delete(long id) throws NotFoundException {
    LOG.trace("delete({})", id);

    int deleted = jdbcTemplate.update(SQL_DELETE, id);

    if (deleted == 0) {
      throw new NotFoundException("No horse found to delete with id " + id);
    } else if (deleted > 1) {
      // This should never happen. If it does, something is wrong with the DB or the way the prepared statement is set up.
      throw new FatalException("Deleted more than one entry in the database");
    }
  }

  @Override
  public List<Horse> search(HorseSearchDto searchFilter) {
    LOG.trace("search({})", searchFilter);

    Function<String, String> like = str -> "%" + str.toLowerCase() + "%";

    var sqlParams = new MapSqlParameterSource();
    String sql = SQL_SEARCH;

    if (searchFilter.name() != null && searchFilter.name() != "") {
      sql += " AND LOWER(name) LIKE :name";
      sqlParams.addValue("name", like.apply(searchFilter.name()));
    }
    if (searchFilter.description() != null && searchFilter.description() != "") {
      sql += " AND LOWER(description) LIKE :description";
      sqlParams.addValue("description", like.apply(searchFilter.description()));
    }
    if (searchFilter.sex() != null) {
      sql += " AND sex = :sex";
      sqlParams.addValue("sex", searchFilter.sex().name());
    }
    if (searchFilter.bornBefore() != null) {
      sql += " AND date_of_birth < :birth";
      sqlParams.addValue("birth", Date.valueOf(searchFilter.bornBefore()));
    }
    if (searchFilter.ownerName() != null && searchFilter.ownerName() != "") {
      sql += " AND owner_id IN (SELECT id FROM owner WHERE LOWER(first_name) LIKE :owner OR LOWER(last_name) LIKE :owner)";
      sqlParams.addValue("owner", like.apply(searchFilter.ownerName()));
    }
    if (searchFilter.limit() != null) {
      sql += " LIMIT :limit";
      sqlParams.addValue("limit", searchFilter.limit());
    }

    return new NamedParameterJdbcTemplate(jdbcTemplate).query(sql, sqlParams, this::mapRow);
  }


  @Override
  public Horse update(HorseDetailDto horse) throws NotFoundException {
    LOG.trace("update({})", horse);
    int updated = jdbcTemplate.update(SQL_UPDATE,
        horse.name(),
        horse.description(),
        horse.dateOfBirth(),
        horse.sex().toString(),
        horse.ownerId(),
        horse.fatherId(),
        horse.motherId(),
        horse.id());
    if (updated == 0) {
      throw new NotFoundException("Could not update horse with ID " + horse.id() + ", because it does not exist");
    }

    return new Horse()
        .setId(horse.id())
        .setName(horse.name())
        .setDescription(horse.description())
        .setDateOfBirth(horse.dateOfBirth())
        .setSex(horse.sex())
        .setOwnerId(horse.ownerId())
        .setFatherId(horse.fatherId())
        .setMotherId(horse.motherId());
  }

  @Override
  public List<Horse> getAncestors(long rootId, long generations) throws NotFoundException {
    LOG.trace("getAncestors({},{})", rootId, generations);

    List<Horse> ancestors;
    try {
      ancestors = jdbcTemplate.query(SQL_GET_ANCESTORS, this::mapRow, rootId, generations);
    } catch (DataAccessException ex) {
      throw new FatalException("The database query errored", ex);
    }

    if (ancestors.isEmpty()) {
      throw new NotFoundException("No horse with ID %d found".formatted(rootId));
    }

    return ancestors;
  }

  @Override
  public boolean isParent(long horseId) {
    LOG.trace("isParent({})", horseId);

    List<Horse> horses;
    try {
      horses = jdbcTemplate.query(SQL_GET_DIRECT_CHILDREN, this::mapRow, horseId, horseId);
    } catch (DataAccessException ex) {
      throw new FatalException("The database query errored", ex);
    }

    return !horses.isEmpty();
  }

  private Horse mapRow(ResultSet result, int rownum) throws SQLException {
    return new Horse()
        .setId(result.getLong("id"))
        .setName(result.getString("name"))
        .setDescription(result.getString("description"))
        .setDateOfBirth(result.getDate("date_of_birth").toLocalDate())
        .setSex(Sex.valueOf(result.getString("sex")))
        .setOwnerId(result.getObject("owner_id", Long.class))
        .setFatherId(result.getObject("father_id", Long.class))
        .setMotherId(result.getObject("mother_id", Long.class));
  }
}
