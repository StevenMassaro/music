create table playlist (
    id serial primary key,
    name varchar(1000) not null,
    dateCreated timestamptz not null,
    dateUpdated timestamptz null
);

create table playlisttrack (
    playlistId int not null references playlist(id) on delete cascade,
    trackId int not null references track(id) on delete cascade,
    sequenceId serial not null,
    dateAdded timestamptz not null,
    primary key (playlistId, trackId)
);
