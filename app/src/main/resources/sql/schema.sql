CREATE TABLE sites (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  host VARCHAR(255) NOT NULL UNIQUE,

  name VARCHAR(255) NOT NULL,
  icon BLOB,

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

CREATE TABLE tutors (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    site INTEGER,
    uuid VARCHAR(36),

    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,

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
  event INTEGER,
  uuid VARCHAR(36),

  name VARCHAR(1024) NOT NULL,

  FOREIGN KEY (event) REFERENCES events(id),
  UNIQUE (event, uuid)
);

CREATE TABLE appointments (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  course INTEGER,
  uuid VARCHAR(36),

  start INTEGER NOT NULL,
  end INTEGER NOT NULL,

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
  tutor INTEGER,
  course INTEGER,

  PRIMARY KEY (tutor, course),
  FOREIGN KEY (tutor) REFERENCES tutors(id),
  FOREIGN KEY (course) REFERENCES courses(id)
);

CREATE TABLE tutor_appointments (
  tutor INTEGER,
  appointment INTEGER,

  PRIMARY KEY (tutor, appointment),
  FOREIGN KEY (tutor) REFERENCES tutors(id),
  FOREIGN KEY (appointment) REFERENCES appointments(id)
);
