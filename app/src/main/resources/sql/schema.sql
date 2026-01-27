
-- TODO: icon, name
CREATE TABLE sites (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  host VARCHAR(255) NOT NULL UNIQUE,

  name VARCHAR(255) NOT NULL,
  icon BLOB,

  fetched INTEGER
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
  uuid VARCHAR(36),

  name VARCHAR(1024) NOT NULL,

  FOREIGN KEY (site) REFERENCES sites(id),
  UNIQUE (site, uuid)
);

CREATE TABLE appointments (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  course INTEGER,
  uuid VARCHAR(36),

  start INTEGER NOT NULL,
  end INTEGER NOT NULL,

 -- TODO: location, tutors

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
