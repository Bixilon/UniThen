CREATE TABLE course_enrolled (
  user INTEGER,
  course INTEGER,

  PRIMARY KEY (user, course),
  FOREIGN KEY (user) REFERENCES users(id),
  FOREIGN KEY (course) REFERENCES courses(id)
);

CREATE TABLE appointment_checkins (
  user INTEGER,
  appointment INTEGER,

  uuid VARCHAR(36) NULL,
  time INTEGER,

  message VARCHAR(1024) NULL,

  sync INTEGER NULL,

  status TEXT CHECK(status IN ('OK', 'FAILED', 'PENDING')),


  PRIMARY KEY (user, appointment),
  FOREIGN KEY (user) REFERENCES users(id),
  FOREIGN KEY (appointment) REFERENCES appointments(id),
  UNIQUE (appointment, uuid)
);

ALTER TABLE appointments
  ADD attendees_fetched INTEGER NULL;
