ALTER TABLE tutors
RENAME TO users
;

ALTER TABLE tutor_courses
RENAME COLUMN tutor TO user
;

ALTER TABLE tutor_appointments
RENAME COLUMN tutor TO user
;
