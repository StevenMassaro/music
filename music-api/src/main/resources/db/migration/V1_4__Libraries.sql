create table library (
    id serial primary key,
    subfolder varchar unique,
    name varchar unique
);

insert into library (subfolder, name) values ('Music', 'Music');

alter table track add column libraryid int references library(id);

update track set libraryid = (
    select id from library where name = 'Music'
);

alter table track alter column libraryid set not null;

--  need to change the unique location constraint to include the library column, because
-- it would be valid to have two files with the same location in different libraries
ALTER TABLE track DROP CONSTRAINT track_pkey;
ALTER TABLE track ADD PRIMARY KEY (location, libraryid);