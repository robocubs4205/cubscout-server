# --- !Ups

CREATE TABLE "districts" (
  "id"        BIGINT GENERATED BY DEFAULT AS IDENTITY ( START WITH 1) NOT NULL PRIMARY KEY,
  "code"      VARCHAR                                                 NOT NULL UNIQUE,
  "name"      VARCHAR                                                 NOT NULL UNIQUE,
  "gameType"  VARCHAR                                                 NOT NULL,
  "firstYear" INT                                                     NOT NULL,
  "lastYear"  INT
);

CREATE TABLE "games" (
  "id"   BIGINT GENERATED BY DEFAULT AS IDENTITY ( START WITH 1) NOT NULL PRIMARY KEY,
  "name" VARCHAR                                                 NOT NULL UNIQUE,
  "type" VARCHAR                                                 NOT NULL,
  "year" INT                                                     NOT NULL,
  CONSTRAINT "type_year_ux" UNIQUE ("type", "year")
);

CREATE TABLE "events" (
  "id"         BIGINT GENERATED BY DEFAULT AS IDENTITY ( START WITH 1) NOT NULL PRIMARY KEY,
  "name"       VARCHAR                                                 NOT NULL,
  "address"    VARCHAR,
  "startDate"  DATE,
  "endDate"    DATE,
  "districtId" BIGINT                                                  NOT NULL,
  "gameId"     BIGINT                                                  NOT NULL,
  FOREIGN KEY ("districtId") REFERENCES "districts" ("id"),
  FOREIGN KEY ("gameId") REFERENCES "games" ("id"),
  CONSTRAINT "game_name_ux" UNIQUE ("gameId", "name")
);

CREATE TABLE "matches" (
  "id"      BIGINT GENERATED BY DEFAULT AS IDENTITY ( START WITH 1) NOT NULL PRIMARY KEY,
  "eventId" BIGINT                                                  NOT NULL,
  "number"  BIGINT                                                  NOT NULL,
  "type"    VARCHAR                                                 NOT NULL,
  FOREIGN KEY ("eventId") REFERENCES "events" ("id"),
  CONSTRAINT "event_number_type_ux" UNIQUE ("eventId", "number", "type")
);

CREATE TABLE "teams" (
  "id"         BIGINT GENERATED BY DEFAULT AS IDENTITY ( START WITH 1) NOT NULL PRIMARY KEY,
  "number"     BIGINT                                                  NOT NULL,
  "name"       VARCHAR,
  "gameType"   VARCHAR                                                 NOT NULL,
  "districtId" BIGINT,
  FOREIGN KEY ("districtId") REFERENCES "districts" ("id"),
  CONSTRAINT "number_gameType_ux" UNIQUE ("number", "gameType")
);

CREATE TABLE "robots" (
  "id"     BIGINT GENERATED BY DEFAULT AS IDENTITY ( START WITH 1) NOT NULL PRIMARY KEY,
  "teamId" BIGINT                                                  NOT NULL,
  "gameId" BIGINT                                                  NOT NULL,
  "name"   VARCHAR,
  FOREIGN KEY ("teamId") REFERENCES "teams" ("id"),
  FOREIGN KEY ("gameId") REFERENCES "games" ("id")
);

CREATE TABLE "teamsInMatches" (
  "teamId"  BIGINT NOT NULL,
  "matchId" BIGINT NOT NULL,
  PRIMARY KEY ("teamId", "matchId")
);

INSERT INTO "districts" ("id", "code", "name", "gameType", "firstYear")
VALUES (1, 'PNW', 'Pacific North West', 'FRC', 2017);

INSERT INTO "games" ("id", "name", "type", "year") VALUES (1, 'SteamWorks', 'FRC', 2017);

INSERT INTO "events" ("id", "name", "startDate", "districtId", "gameId") VALUES (1, 'Glacier Peak', '2016-01-01', 1, 1);

INSERT INTO "teams" ("id", "number", "name", "gameType", "districtId") VALUES (1, 4205, 'RoboCubs', 'FRC', 1);

INSERT INTO "robots" ("id", "teamId", "gameId", "name") VALUES (1, 1, 1, 'Ruby');

INSERT INTO "matches" ("id", "eventId", "number", "type") VALUES (1, 1, 1, 'Qualifying');

INSERT INTO "teamsInMatches" ("teamId", "matchId") VALUES (1, 1);

# --- !Downs

DROP TABLE "districts" IF EXISTS;
DROP TABLE "events" IF EXISTS;
DROP TABLE "games" IF EXISTS;
DROP TABLE "matches" IF EXISTS;
DROP TABLE "robots" IF EXISTS;
DROP TABLE "teams" IF EXISTS;
DROP TABLE "teamsInMatches" IF EXISTS