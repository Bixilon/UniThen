INSERT INTO sites(id, host, name, fetched) VALUES (901, "test.local", "First dummy site", 0);
INSERT INTO sites(id, host, name, fetched) VALUES (902, "test2.local", "Second dummy site", 0);


INSERT INTO events(id, site, uuid, name, start, end) VALUES(901, 901, "00000000-0000-0000-0000-000000000001", "Test Event (a)", 1767312123, 1893542523);
INSERT INTO events(id, site, uuid, name, start, end) VALUES(902, 901, "00000000-0000-0000-0000-000000000002", "Test Event (b)", 1767312123, 1893542523);
INSERT INTO events(id, site, uuid, name, start, end) VALUES(903, 902, "00000000-0000-0000-0000-000000000001", "2Test Event (a)", 1767312123, 1893542523);
INSERT INTO events(id, site, uuid, name, start, end) VALUES(904, 902, "00000000-0000-0000-0000-000000000003", "2Test Event (b)", 1767312123, 1893542523);

INSERT INTO users(id, site, uuid, firstname, lastname) VALUES(901, 901, "00000000-0000-0000-0000-000000000001", "Hans", "Maulwurf");
INSERT INTO users(id, site, uuid, firstname, lastname) VALUES(902, 901, "00000000-0000-0000-0000-000000000002", "Peter", "Wurst");
INSERT INTO users(id, site, uuid, firstname, lastname) VALUES(903, 901, "00000000-0000-0000-0000-000000000003", "Emilia", "Gans");
INSERT INTO users(id, site, uuid, firstname, lastname) VALUES(904, 901, "00000000-0000-0000-0000-000000000004", "Gustaf", "Maier");
INSERT INTO users(id, site, uuid, firstname, lastname) VALUES(905, 901, "00000000-0000-0000-0000-000000000005", "Hannah", "Lang");

INSERT INTO users(id, site, uuid, firstname, lastname) VALUES(906, 902, "00000000-0000-0000-0000-000000000001", "Mia", "Lang");
INSERT INTO users(id, site, uuid, firstname, lastname) VALUES(907, 902, "00000000-0000-0000-0000-000000000006", "Marie", "Zimmer");
INSERT INTO users(id, site, uuid, firstname, lastname) VALUES(908, 902, "00000000-0000-0000-0000-000000000007", "Sophie", "Kurz");


INSERT INTO accounts(id, site, uuid, firstname, lastname, fetched, session_key) VALUES(901, 901, "00000000-0000-0000-0000-000000000001", "Hans", "Maulwurf", 1, "a");
INSERT INTO accounts(id, site, uuid, firstname, lastname, fetched, session_key) VALUES(902, 901, "00000000-0000-0000-0000-000000000002", "Peter", "Wurst", 1, "a");
INSERT INTO accounts(id, site, uuid, firstname, lastname, fetched, session_key) VALUES(903, 902, "00000000-0000-0000-0000-000000000002", "Marie", "Zimmer", 1, "");


INSERT INTO courses(id, site, event, uuid, name) VALUES(901, 901, 901, "00000000-0000-0000-0000-000000000001", "First course");
INSERT INTO courses(id, site, event, uuid, name) VALUES(902, 901, 901, "00000000-0000-0000-0000-000000000002", "Second course");
INSERT INTO courses(id, site, event, uuid, name) VALUES(903, 902, 902, "00000000-0000-0000-0000-000000000001", "First course B");

INSERT INTO courses(id, site, event, uuid, name) VALUES(904, 902, 902, "00000000-0000-0000-0000-000000000005", "Unreferenced course");



INSERT INTO appointments(id, course, uuid, start, end, location) VALUES(901, 901, "00000000-0000-0000-0000-000000000001", 1767312123, 1893542523, "Somewhere");
INSERT INTO appointments(id, course, uuid, start, end, location) VALUES(902, 902, "00000000-0000-0000-0000-000000000002", 1769446900, 1769446940, "Somewhere");
INSERT INTO appointments(id, course, uuid, start, end, location) VALUES(903, 902, "00000000-0000-0000-0000-000000000003", 1769446950, 1769446980, "Somewhere");


INSERT INTO account_courses(account, course) VALUES(901, 901);
INSERT INTO account_courses(account, course) VALUES(901, 902);
INSERT INTO account_courses(account, course) VALUES(902, 903);
INSERT INTO account_courses(account, course) VALUES(903, 901);
INSERT INTO account_courses(account, course) VALUES(903, 902);
INSERT INTO account_courses(account, course) VALUES(903, 903);

INSERT INTO tutor_courses(user, course) VALUES(901, 901);
INSERT INTO tutor_courses(user, course) VALUES(905, 902);

INSERT INTO tutor_appointments(user, appointment) VALUES(901, 901);
INSERT INTO tutor_appointments(user, appointment) VALUES(905, 902);

INSERT INTO course_enrolled(user, course) VALUES(902, 901);
INSERT INTO course_enrolled(user, course) VALUES(903, 901);
INSERT INTO course_enrolled(user, course) VALUES(904, 901);
INSERT INTO course_enrolled(user, course) VALUES(905, 901);
INSERT INTO course_enrolled(user, course) VALUES(901, 902);
