-- liquibase formatted sql

-- changeset sbutramev:1
CREATE INDEX student_name_index ON student (name);

-- changeset sbutramev:2
CREATE INDEX faculty_nc_index ON faculty (name, color);