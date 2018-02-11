CREATE TABLE IF NOT EXISTS data (
  id       BIGINT      NOT NULL auto_increment,
  time     DATETIME(3) NOT NULL,
  value    FLOAT       NOT NULL,
  probe_id INTEGER,
  PRIMARY KEY (id)
) engine = MyIsam;
CREATE TABLE IF NOT EXISTS node (
  id      INTEGER NOT NULL auto_increment,
  address VARCHAR(255),
  mode    VARCHAR(255),
  name    VARCHAR(255),
  PRIMARY KEY (id)
) engine = MyIsam;
CREATE TABLE IF NOT EXISTS probe (
  id      INTEGER NOT NULL auto_increment,
  name    VARCHAR(255),
  type    VARCHAR(255),
  node_id INTEGER,
  PRIMARY KEY (id)
) engine = MyIsam;
ALTER TABLE data
  ADD CONSTRAINT FKadlf587q1pfo283tqx4jylta4 FOREIGN KEY (probe_id) REFERENCES probe (id);
ALTER TABLE probe
  ADD CONSTRAINT FKpnvpfnkq2x6b8m4mh5m3lqgje FOREIGN KEY (node_id) REFERENCES node (id);
