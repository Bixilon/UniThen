CREATE VIRTUAL TABLE users_fts USING fts4(fullname, tokenize=unicode61);

INSERT INTO users_fts(docid, fullname)
SELECT id, firstname || ' ' || lastname
FROM users;


CREATE TRIGGER users_fts_bu BEFORE UPDATE ON users
BEGIN
  DELETE FROM users_fts WHERE docid=old.id;
END;
CREATE TRIGGER users_fts_bd BEFORE DELETE ON users
BEGIN
  DELETE FROM users_fts WHERE docid=old.id;
END;

CREATE TRIGGER users_fts_au AFTER UPDATE ON users
BEGIN
  INSERT INTO users_fts(docid, fullname) VALUES (new.id, new.firstname || ' ' || new.lastname);
END;
CREATE TRIGGER users_fts_ai AFTER INSERT ON users
BEGIN
  INSERT INTO users_fts(docid, fullname) VALUES (new.id, new.firstname || ' ' || new.lastname);
END;
