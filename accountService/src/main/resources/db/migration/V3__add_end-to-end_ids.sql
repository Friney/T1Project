alter table client
    add column client_id bigint unique;

update client
set client_id = id
where client_id is null;

alter table client
    alter column client_id set not null;

alter table account
    add column account_id bigint unique;

update account
set account_id = id
where account_id is null;

alter table account
    alter column account_id set not null;

alter table transaction
    add column transaction_id bigint unique;

update transaction
set transaction_id = id
where transaction_id is null;

alter table transaction
    alter column transaction_id set not null;

create sequence client_end_to_end_id_seq;

create sequence account_end_to_end_id_seq;

create sequence transaction_end_to_end_id_seq;
