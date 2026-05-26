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
