-- insert initial test data
-- the IDs are hardcoded to enable references between further test data
-- negative IDs are used to not interfere with user-entered data and allow clean deletion of test data

DELETE FROM horse where id < 0;

INSERT INTO horse (id, name, description, date_of_birth, sex, owner_id, father_id, mother_id)
VALUES
    (-1, 'Wendy', 'The famous one!', '2012-12-12', 'FEMALE', NULL, NULL, NULL),
    (-2, 'Tom', 'The famous father!', '2012-12-12', 'MALE', NULL, NULL, NULL),
    (-3, 'Paul', 'The famous son!', '2016-12-12', 'MALE', NULL, -2, -1)
;
