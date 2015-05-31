CREATE TABLE resources (
  id serial PRIMARY KEY,
  url character varying(255) NOT NULL UNIQUE,
  label character varying(255),
  processed boolean NOT NULL DEFAULT false
);

CREATE TABLE resources_parents (
  id serial PRIMARY KEY,
  child_resource_id integer NOT NULL REFERENCES resources (id) ON DELETE CASCADE,
  parent_resource_id integer NOT NULL REFERENCES resources (id) ON DELETE CASCADE,
  UNIQUE (child_resource_id, parent_resource_id)
);

CREATE TABLE resources_sameas (
  id serial PRIMARY KEY,
  first_resource_id integer NOT NULL REFERENCES resources (id) ON DELETE CASCADE,
  second_resource_id integer NOT NULL REFERENCES resources (id) ON DELETE CASCADE,
  UNIQUE(first_resource_id, second_resource_id)
);
