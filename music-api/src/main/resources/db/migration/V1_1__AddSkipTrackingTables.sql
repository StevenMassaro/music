create table skips
(
	id            serial    not null,
	songid        int       not null references track (id),
	deviceid      int       not null references device (id),
	skipdate      timestamp not null,
	imported      boolean   not null default false,
	secondsPlayed real,
	primary key (songid, deviceid, skipdate)
);

create table skipcount
(
	songid    int     not null references track (id),
	deviceid  int     not null references device (id),
	playcount int     not null,
	imported  boolean not null default true,
	primary key (songid, deviceid)
);