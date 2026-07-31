CREATE TABLE login_flows
(
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    site           INTEGER,

    exchange_token VARCHAR(1024) NULL,

    expires        INTEGER,

    FOREIGN KEY (site) REFERENCES sites (id)
);
