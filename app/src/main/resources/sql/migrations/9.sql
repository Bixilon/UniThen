ALTER TABLE appointments
RENAME COLUMN attendees_fetched TO fetched_attendees
;

ALTER TABLE courses
  ADD fetched_enrolled INTEGER NULL
  ;
