CREATE TABLE sample
(
  id          SERIAL PRIMARY KEY NOT NULL,
  node_id     INTEGER,
  sample_time TIMESTAMP(6)       NOT NULL,
  value       DOUBLE PRECISION   NOT NULL,
  CONSTRAINT sample_node_id_fk FOREIGN KEY (node_id) REFERENCES node (id)
);
CREATE INDEX sample_node_id_sample_time_index
  ON sample
  USING BTREE
  (node_id, sample_time);