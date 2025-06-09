alter table client
    add column status int;

update client
set status = 2
where status is null;
