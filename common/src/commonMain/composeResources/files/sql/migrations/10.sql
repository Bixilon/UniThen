ALTER TABLE account_courses
  ADD tutor INTEGER DEFAULT 0
;

UPDATE account_courses SET tutor = 1 WHERE EXISTS (
SELECT 1 FROM tutor_courses
INNER JOIN users ON tutor_courses."user" = users.id
INNER JOIN accounts ON (account_courses.account = accounts.id AND users.uuid = accounts.uuid AND users.site = accounts.site)
WHERE tutor_courses.course = account_courses.course
);
