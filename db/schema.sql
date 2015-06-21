CREATE TABLE resources (
  id serial PRIMARY KEY,
  url character varying(255) NOT NULL UNIQUE,
  label character varying(255),
  processed_at timestamp without time zone,
  level integer,
  hits integer
);

CREATE TABLE entities_mappings (
  id serial PRIMARY KEY,
  entity_id integer NOT NULL,
  resource_id integer NOT NULL REFERENCES resources (id),
  UNIQUE(entity_id, resource_id)
);

CREATE TABLE resources_parents (
  id serial PRIMARY KEY,
  child_resource_id integer NOT NULL REFERENCES resources (id) ON DELETE CASCADE,
  parent_resource_id integer NOT NULL REFERENCES resources (id) ON DELETE CASCADE,
  UNIQUE (child_resource_id, parent_resource_id),
  CHECK (child_resource_id <> parent_resource_id)
);

CREATE TABLE resources_sameas (
  id serial PRIMARY KEY,
  first_resource_id integer NOT NULL REFERENCES resources (id) ON DELETE CASCADE,
  second_resource_id integer NOT NULL REFERENCES resources (id) ON DELETE CASCADE,
  UNIQUE(first_resource_id, second_resource_id)
);

CREATE TABLE dimensions  (
  id serial PRIMARY KEY,
  entity_id integer NOT NULL,
  level_name character varying(255),
  level_distance integer NOT NULL
);
