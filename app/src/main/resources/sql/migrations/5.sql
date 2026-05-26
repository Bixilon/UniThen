CREATE TABLE course_enrolled (
  user INTEGER,
  course INTEGER,

  PRIMARY KEY (user, course),
  FOREIGN KEY (user) REFERENCES users(id),
  FOREIGN KEY (course) REFERENCES courses(id)
);

CREATE TABLE appointment_attendee (
  user INTEGER,
  course INTEGER,

  PRIMARY KEY (user, course),
  FOREIGN KEY (user) REFERENCES users(id),
  FOREIGN KEY (course) REFERENCES courses(id)
);

ALTER TABLE users
RENAME COLUMN first_name TO firstname,
RENAME COLUMN last_name TO lastname,
;
