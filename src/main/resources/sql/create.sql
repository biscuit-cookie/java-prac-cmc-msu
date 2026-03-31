DROP TABLE IF EXISTS person_place CASCADE;
DROP TABLE IF EXISTS relation CASCADE;
DROP TABLE IF EXISTS place CASCADE;
DROP TABLE IF EXISTS person CASCADE;

CREATE TABLE person (
    person_id bigserial PRIMARY KEY,
    person_name text NOT NULL,
    gender text NOT NULL,
    date_of_birth integer,
    date_of_death integer,
    characteristics text,
    CONSTRAINT person_dates_check
        CHECK (date_of_birth IS NULL OR date_of_death IS NULL OR date_of_birth <= date_of_death)
);

CREATE TABLE place (
    place_id bigserial PRIMARY KEY,
    name text NOT NULL,
    description text NOT NULL
);

CREATE TABLE relation (
    relation_id bigserial PRIMARY KEY,
    target_person bigint NOT NULL REFERENCES person (person_id) ON DELETE CASCADE,
    related_person bigint NOT NULL REFERENCES person (person_id) ON DELETE CASCADE,
    date_of_beginning integer,
    date_of_end integer,
    relationship_type integer NOT NULL,
    CONSTRAINT relation_dates_check
        CHECK (date_of_beginning IS NULL OR date_of_end IS NULL OR date_of_beginning <= date_of_end),
    CONSTRAINT relation_type_check
        CHECK (relationship_type BETWEEN 0 AND 3),
    CONSTRAINT different_people_check
        CHECK (target_person <> related_person)
);

CREATE TABLE person_place (
    person_id bigint NOT NULL REFERENCES person (person_id) ON DELETE CASCADE,
    place_id bigint NOT NULL REFERENCES place (place_id) ON DELETE CASCADE,
    PRIMARY KEY (place_id, person_id)
);
