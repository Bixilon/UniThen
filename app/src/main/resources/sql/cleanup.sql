CREATE TEMP TABLE temp.orphan_courses AS
SELECT courses.id
FROM courses
LEFT JOIN account_courses ON account_courses.course = courses.id
WHERE account_courses.course IS NULL;

DELETE FROM tutor_appointments
WHERE appointment IN (SELECT id FROM appointments WHERE course IN temp.orphan_courses);

DELETE FROM appointment_attendees
WHERE appointment IN (SELECT id FROM appointments WHERE course IN temp.orphan_courses);

DELETE FROM checkin_queue
WHERE appointment IN (SELECT id FROM appointments WHERE course IN temp.orphan_courses);

DELETE FROM course_enrolled WHERE course IN temp.orphan_courses;
DELETE FROM tutor_courses WHERE course IN temp.orphan_courses;
DELETE FROM appointments WHERE course IN temp.orphan_courses;

DELETE FROM courses WHERE id IN temp.orphan_courses;

DELETE FROM users
WHERE id NOT IN (
    SELECT user FROM checkin_queue
    UNION
    SELECT user FROM tutor_courses
    UNION
    SELECT user FROM tutor_appointments
    UNION
    SELECT user FROM course_enrolled
    UNION
    SELECT user FROM appointment_attendees
    UNION
    SELECT user FROM accounts
);

DROP TABLE temp.orphan_courses;
