create table if not exists client
(
    id         bigserial primary key,
    first_name varchar(255) not null,
    middle_name varchar(255),
    last_name  varchar(255) not null
);

create table if not exists account
(
    id           bigserial primary key,
    account_type int not null,
    balance      decimal(19, 2) not null,
    client_id    bigint not null,
    foreign key (client_id) references client (id)  on delete cascade
);

create table if not exists transaction
(
    id              bigserial primary key,
    amount          decimal(19, 2) not null,
    transaction_time timestamp not null,
    account_id      bigint not null,
    foreign key (account_id) references account (id)  on delete cascade
);

create table if not exists data_source_error_log
(
    id               bigserial primary key,
    stack_trace      text not null,
    error_message    text not null,
    method_signature text not null
);