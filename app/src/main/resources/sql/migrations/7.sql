CREATE VIRTUAL TABLE users_fts USING fts4(fullname, tokenize=unicode61);

INSERT INTO users_fts(docid, fullname)
SELECT id, firstname || ' ' || lastname
FROM users;

CREATE TRIGGER users_fts_insert AFTER INSERT ON users
BEGIN
    INSERT INTO users_fts(docid, fullname)
    VALUES (new.id, new.firstname || ' ' || new.lastname);
END;

CREATE TRIGGER users_fts_update AFTER UPDATE ON users
BEGIN
    INSERT INTO users_fts(users_fts, docid, fullname)
    VALUES('delete', old.id, old.firstname || ' ' || old.lastname);

    INSERT INTO users_fts(docid, fullname)
    VALUES(new.id, new.firstname || ' ' || new.lastname);
END;

CREATE TRIGGER users_fts_delete AFTER DELETE ON users
BEGIN
    INSERT INTO users_fts(users_fts, docid, fullname)
    VALUES('delete', old.id, old.firstname || ' ' || old.lastname);
END;
