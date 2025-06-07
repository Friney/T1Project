alter table transaction
    add column status      int,
    add column create_time timestamp;

update transaction
set status = 0
where status is null;

alter table transaction
    alter column status set not null;

update transaction
set create_time = now()
where create_time is null;

alter table account
    add column status        int,
    add column frozen_amount decimal(19, 2);

update account
set status = 3
where status is null;

alter table account
    alter column status set not null;

update account
set frozen_amount = 0
where frozen_amount is null;

alter table account
    alter column frozen_amount set not null;
