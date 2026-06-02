DROP TABLE appointment_checkins; -- TODO: Not correct, but data is cached and no release was made.

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
