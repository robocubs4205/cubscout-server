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
  CONSTRAINT "type_year_ux_games" UNIQUE ("type", "year")
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
  CONSTRAINT "game_name_ux_events" UNIQUE ("gameId", "name")
);

CREATE TABLE "matches" (
  "id"      BIGINT GENERATED BY DEFAULT AS IDENTITY ( START WITH 1) NOT NULL PRIMARY KEY,
  "eventId" BIGINT                                                  NOT NULL,
  "number"  BIGINT                                                  NOT NULL,
  "type"    VARCHAR                                                 NOT NULL,
  FOREIGN KEY ("eventId") REFERENCES "events" ("id"),
  CONSTRAINT "event_number_type_ux_matches" UNIQUE ("eventId", "number", "type")
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
  FOREIGN KEY ("gameId") REFERENCES "games" ("id"),
  CONSTRAINT "team_game_ux" UNIQUE ("teamId", "gameId")
);

CREATE TABLE "teamsInMatches" (
  "robotId" BIGINT NOT NULL,
  "matchId" BIGINT NOT NULL,
  PRIMARY KEY ("robotId", "matchId")
);

INSERT INTO "districts" ("id", "code", "name", "gameType", "firstYear")
VALUES (1, 'PNW', 'Pacific North West', 'FRC', 2017);

INSERT INTO "games" ("id", "name", "type", "year") VALUES (1, 'SteamWorks', 'FRC', 2017);

INSERT INTO "events" ("id", "name", "startDate", "districtId", "gameId") VALUES (1, 'Glacier Peak', '2016-01-01', 1, 1);

INSERT INTO "teams" ("id", "number", "name", "gameType", "districtId") VALUES (1, 4205, 'RoboCubs', 'FRC', 1);

INSERT INTO "robots" ("id", "teamId", "gameId", "name") VALUES (1, 1, 1, 'Ruby');

INSERT INTO "matches" ("id", "eventId", "number", "type") VALUES (1, 1, 1, 'Qualifying');

INSERT INTO "teamsInMatches" ("robotId", "matchId") VALUES (1, 1);

CREATE TABLE "users" (
  "id"             VARCHAR NOT NULL PRIMARY KEY,
  "username"       VARCHAR NOT NULL,
  "hashedPassword" VARCHAR NOT NULL
);

CREATE TABLE "serverClients" (
  "id"     VARCHAR NOT NULL PRIMARY KEY,
  "name"   VARCHAR NOT NULL,
  "author" VARCHAR NOT NULL,
  "secret" VARCHAR NOT NULL,
  "uris"   VARCHAR NOT NULL
);

CREATE TABLE "browserClients" (
  "id"     VARCHAR NOT NULL PRIMARY KEY,
  "name"   VARCHAR NOT NULL,
  "author" VARCHAR NOT NULL,
  "uris"   VARCHAR NOT NULL
);

CREATE TABLE "nativeClients" (
  "id"     VARCHAR NOT NULL PRIMARY KEY,
  "name"   VARCHAR NOT NULL,
  "author" VARCHAR NOT NULL,
  "uris"   VARCHAR NOT NULL
);

CREATE TABLE "firstPartyClients" (
  "id"     VARCHAR NOT NULL PRIMARY KEY,
  "name"   VARCHAR NOT NULL,
  "secret" VARCHAR,
  "uris"   VARCHAR NOT NULL
);

CREATE TABLE "refreshTokens" (
  "selector"  VARCHAR NOT NULL PRIMARY KEY,
  "validator" VARCHAR NOT NULL,
  "clientId"  VARCHAR NOT NULL,
  "userId"    VARCHAR NOT NULL,
  "scopes"    VARCHAR NOT NULL,
  FOREIGN KEY ("userId") REFERENCES "users" ("id"),
);

CREATE TABLE "accessTokensWithRefreshTokens" (
  "selector"             VARCHAR   NOT NULL PRIMARY KEY,
  "validator"            VARCHAR   NOT NULL,
  "refreshTokenSelector" VARCHAR   NOT NULL,
  "created"              TIMESTAMP NOT NULL,
  FOREIGN KEY ("refreshTokenSelector") REFERENCES "refreshTokens" ("selector")
);

CREATE TABLE "standaloneAccessTokens" (
  "selector"  VARCHAR   NOT NULL PRIMARY KEY,
  "validator" VARCHAR   NOT NULL,
  "created"   TIMESTAMP NOT NULL,
  "clientId"  VARCHAR   NOT NULL,
  "userId"    VARCHAR   NOT NULL,
  "scopes"    VARCHAR   NOT NULL,
  FOREIGN KEY ("userId") REFERENCES "users" ("id"),
);

CREATE TABLE "authCodes" (
  "selector"    VARCHAR NOT NULL PRIMARY KEY,
  "validator"   VARCHAR NOT NULL,
  "clientId"    VARCHAR NOT NULL,
  "userId"      VARCHAR NOT NULL,
  "redirectURL" VARCHAR NOT NULL,
  FOREIGN KEY ("userId") REFERENCES "users" ("id"),
);

INSERT INTO "firstPartyClients" VALUES ('AAAAAAAAAAAAAAAAAAAAAA','foo', NULL, '["localhost:9000"]');

insert into "users" values ('AAAAAAAAAAAAAAAAAAAAAA','bob','$2a$04$XRdsQ5edYQOdA03dt6O0TO16vWota06WN3Nk68xi0OLcz0zURnq3a')

# --- !Downs

DROP TABLE IF EXISTS "districts";
DROP TABLE IF EXISTS "events";
DROP TABLE IF EXISTS "games";
DROP TABLE IF EXISTS "matches";
DROP TABLE IF EXISTS "robots";
DROP TABLE IF EXISTS "teams";
DROP TABLE IF EXISTS "teamsInMatches";

DROP TABLE IF EXISTS "serverClients";
DROP TABLE IF EXISTS "browserClients";
DROP TABLE IF EXISTS "nativeClients";
DROP TABLE IF EXISTS "firstPartyClients";

DROP TABLE IF EXISTS "users";
DROP TABLE IF EXISTS "refreshTokens";
DROP TABLE IF EXISTS "accessTokensWithRefreshTokens";
DROP TABLE IF EXISTS "standaloneAccessTokens";
DROP TABLE IF EXISTS "authCodes";