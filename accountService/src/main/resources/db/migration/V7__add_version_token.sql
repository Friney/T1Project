create table if not exists version_jwt_user
(
    id      bigserial primary key,
    version bigint not null,
    user_id bigint not null,
    foreign key (user_id) references "user" (id) on delete cascade
);
