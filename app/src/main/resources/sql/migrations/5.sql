CREATE TABLE course_enrolled (
  user INTEGER,
  course INTEGER,

  PRIMARY KEY (user, course),
  FOREIGN KEY (user) REFERENCES users(id),
  FOREIGN KEY (course) REFERENCES courses(id)
);

CREATE TABLE appointment_attendee (
  user INTEGER,
  appointment INTEGER,

  PRIMARY KEY (user, appointment),
  FOREIGN KEY (user) REFERENCES users(id),
  FOREIGN KEY (appointment) REFERENCES appointments(id)
);

CREATE TABLE appointment_checkins (
  user INTEGER,
  appointment INTEGER,

  time INTEGER NULL,
  uuid VARCHAR(36) NULL,

  message VARCHAR(1024) NULL,

  sync INTEGER NULL,

  status TEXT CHECK(status IN ('OK', 'FAILED', 'PENDING')),


  PRIMARY KEY (user, appointment),
  FOREIGN KEY (user) REFERENCES users(id),
  FOREIGN KEY (appointment) REFERENCES appointments(id),
  UNIQUE (appointment, uuid)
);

ALTER TABLE users
RENAME COLUMN first_name TO firstname,
RENAME COLUMN last_name TO lastname,
;
