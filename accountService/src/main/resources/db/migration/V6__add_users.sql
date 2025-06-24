create table if not exists "user"
(
    id       bigserial primary key,
    login    varchar(255) not null unique,
    password varchar(255) not null
);