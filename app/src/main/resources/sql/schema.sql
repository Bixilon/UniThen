CREATE TABLE sites (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  host VARCHAR(255) NOT NULL UNIQUE,

  name VARCHAR(255) NOT NULL,
  icon BLOB NULL,

  fetched INTEGER
);

CREATE TABLE events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    site INTEGER,
    uuid VARCHAR(36),

    name VARCHAR(255) NOT NULL,

    start INTEGER,
    end INTEGER,

    FOREIGN KEY (site) REFERENCES sites(id),
    UNIQUE (site, uuid)
);

CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    site INTEGER,
    uuid VARCHAR(36),

    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,

    FOREIGN KEY (site) REFERENCES sites(id),
    UNIQUE (site, uuid)
);


CREATE TABLE accounts (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  site INTEGER,
  uuid VARCHAR(36),

  firstname VARCHAR(255) NOT NULL,
  lastname VARCHAR(255) NOT NULL,

  session_key VARCHAR(1024),

  fetched INTEGER,

  FOREIGN KEY (site) REFERENCES sites(id),
  UNIQUE (site, uuid)
);

CREATE TABLE courses (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  site INTEGER,
  event INTEGER,
  uuid VARCHAR(36),

  name VARCHAR(1024) NOT NULL,

  fetched INTEGER,

  FOREIGN KEY (site) REFERENCES sites(id),
  FOREIGN KEY (event) REFERENCES events(id),
  UNIQUE (event, uuid)
);

CREATE TABLE appointments (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  course INTEGER,
  uuid VARCHAR(36),

  start INTEGER NOT NULL,
  end INTEGER NOT NULL,

  canceled INTEGER NULL,

  attendees_fetched INTEGER NULL,

  location VARCHAR(255),

  FOREIGN KEY (course) REFERENCES courses(id),
  UNIQUE (course, uuid)
);

CREATE TABLE account_courses (
  account INTEGER,
  course INTEGER,

  PRIMARY KEY (account, course),
  FOREIGN KEY (account) REFERENCES accounts(id),
  FOREIGN KEY (course) REFERENCES courses(id)
);

CREATE TABLE tutor_courses (
  user INTEGER,
  course INTEGER,

  PRIMARY KEY (user, course),
  FOREIGN KEY (user) REFERENCES users(id),
  FOREIGN KEY (course) REFERENCES courses(id)
);

CREATE TABLE tutor_appointments (
  user INTEGER,
  appointment INTEGER,

  PRIMARY KEY (user, appointment),
  FOREIGN KEY (user) REFERENCES users(id),
  FOREIGN KEY (appointment) REFERENCES appointments(id)
);

CREATE TABLE course_enrolled (
  user INTEGER,
  course INTEGER,

  PRIMARY KEY (user, course),
  FOREIGN KEY (user) REFERENCES users(id),
  FOREIGN KEY (course) REFERENCES courses(id)
);

CREATE TABLE appointment_attendees (
  user INTEGER,
  appointment INTEGER,

  attempt VARCHAR(36) NULL,

  PRIMARY KEY (user, appointment),
  FOREIGN KEY (user) REFERENCES users(id),
  FOREIGN KEY (appointment) REFERENCES appointments(id)
);

CREATE TABLE checkin_queue (
  user INTEGER,
  appointment INTEGER,

  time INTEGER,
  attempt VARCHAR(36) NULL, -- If attempt is set, we check out the user again
  message VARCHAR(1024) NULL, -- If message is set, it failed

  sync INTEGER NULL,


  PRIMARY KEY (user, appointment),
  FOREIGN KEY (user) REFERENCES users(id),
  FOREIGN KEY (appointment) REFERENCES appointments(id)
);

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
