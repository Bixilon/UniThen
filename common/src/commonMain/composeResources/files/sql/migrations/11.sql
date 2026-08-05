CREATE TABLE login_flows(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    site INTEGER,

    exchange_token VARCHAR(1024) NULL,

    expires INTEGER,

    FOREIGN KEY (site) REFERENCES sites (id)
);


-- ALTER TABLE accounts RENAME COLUMN session_key TO authentication (is only supported in sqlite 3.25+)

CREATE TABLE accounts2 (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  site INTEGER,
  uuid VARCHAR(36),

  firstname VARCHAR(255) NOT NULL,
  lastname VARCHAR(255) NOT NULL,

  authentication VARCHAR(1024),

  fetched INTEGER,

  FOREIGN KEY (site) REFERENCES sites(id),
  UNIQUE (site, uuid)
);

INSERT INTO accounts2(id, site, uuid, firstname, lastname, authentication, fetched) SELECT id, site, uuid, firstname, lastname, session_key, fetched FROM accounts;
DROP TABLE accounts;
ALTER TABLE accounts2 RENAME TO accounts;
