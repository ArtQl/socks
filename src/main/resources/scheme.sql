create table if not exists socks
(
    id                serial primary key,
    color             varchar(128) not null,
    cotton_percentage float        not null check (cotton_percentage >= 0 and cotton_percentage <= 100),
    quantity          int          not null check ( quantity >= 0 )
);

insert into socks (color, cotton_percentage, quantity)
values ('red', 80, 100);