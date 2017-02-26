CREATE TABLE node
(
  id SERIAL PRIMARY KEY NOT NULL,
  title VARCHAR NOT NULL,
  authentication_token VARCHAR NOT NULL,
  time_zone VARCHAR NOT NULL
);
CREATE UNIQUE INDEX node_authentication_token_uindex ON node (authentication_token);
