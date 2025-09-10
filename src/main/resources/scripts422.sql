CREATE TABLE person (
    id int PRIMARY KEY,
    name varchar(50),
    age int,
    has_driver_license boolean
);
------------------------------------------------------

CREATE TABLE car (
    id int PRIMARY KEY,
    brand varchar(50),
    model varchar(50),
    price decimal(10, 2)
);
------------------------------------------------------

CREATE TABLE personCar (
    person_id int,
    car_id int,
    PRIMARY KEY (person_id, car_id),
    FOREIGN KEY (person_id) REFERENCES person (id),
    FOREIGN KEY (car_id) REFERENCES car (id)
);