ALTER TABLE courses ADD COLUMN site INTEGER NOT NULL DEFAULT 0;
ALTER TABLE courses ADD COLUMN fetched INTEGER NULL DEFAULT 0;

UPDATE courses SET site = (
    SELECT events.site
    FROM events
    WHERE events.id = courses.event
);

ALTER TABLE courses RENAME TO courses_old;

CREATE TABLE courses (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  site INTEGER,
  event INTEGER,
  uuid VARCHAR(36),

  name VARCHAR(1024) NOT NULL,

  fetched INTEGER,

  FOREIGN KEY (site) REFERENCES sites(id),
  FOREIGN KEY (event) REFERENCES events(id),
  UNIQUE (event, uuid)
);

INSERT INTO courses (id, site, event, uuid, name, fetched)
SELECT id, site, event, uuid, name, fetched FROM courses_old;

-- TODO: Test


DROP TABLE courses_old;

