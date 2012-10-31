-- Table: test_bean

-- DROP TABLE test_bean;

CREATE TABLE test_bean
(
  string_prop text NOT NULL,
  long_prop bigint,
  timets timestamp without time zone,
  CONSTRAINT string_prop_key PRIMARY KEY (string_prop)
)
WITH (
  OIDS=FALSE
);


-- Table: parent_bean

-- DROP TABLE parent_bean;

CREATE TABLE parent_bean
(
  id text,
  test text
)
WITH (
  OIDS=FALSE
);


-- Table: grand_parent_bean

-- DROP TABLE grand_parent_bean;

CREATE TABLE grand_parent_bean
(
  id text,
  parent text
)
WITH (
  OIDS=FALSE
);

CREATE TABLE lock_bean
(
  id text NOT NULL,
  long_prop bigint,
  timets timestamp without time zone,
  version integer,
  CONSTRAINT lock_bean_key PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
