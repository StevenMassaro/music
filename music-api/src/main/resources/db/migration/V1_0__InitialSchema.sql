create table track
(
	id                   serial        not null unique,
	title                varchar(1000) not null,
	location             varchar primary key,
	hash                 varchar(128)  not null,
	album                varchar(1000),
	artist               varchar(1000),
	album_artist         varchar(1000),
	genre                varchar(1000),
	year                 varchar(1000),
	disc_no              int,
	track                int,
	rating               smallint
		constraint within_range check ((rating > 0 and rating < 10) or rating = null),
	comment              varchar,
	bitrate              int           not null,
	encoding             varchar,
	sampleRate           int           not null,
	duration             int           not null,
	deletedInd           boolean       not null,
	dateCreated          timestamp DEFAULT now(),
	dateUpdated          timestamp,
	fileLastModifiedDate timestamp
);

create table smartplaylist
(
	id          serial unique        not null,
	name        varchar(1000) unique not null,
	dynamicsql  varchar              not null,
	dateCreated timestamp DEFAULT now(),
	dateUpdated timestamp
);

create table updatetype
(
	id   serial unique not null,
	name varchar primary key
);

insert into updatetype (id, name)
values (1, 'ID3 tag update');

create table trackupdates
(
	id         serial  not null unique,
	songid     int     not null references track (id),
	field      varchar not null,
	newvalue   varchar not null,
	updatetype int     not null references updatetype (id)
);

create table device
(
	id          serial primary key,
	name        varchar not null unique,
	format      varchar,
	bitrate     int,
	sampleRate  int,
	channels    int,
	artsize     int,
	dateCreated timestamp DEFAULT now()
);

create table hash
(
	trackid     int     not null references track (id),
	deviceid    int     not null references device (id),
	hash        varchar not null,
	dateCreated timestamp DEFAULT now(),
	dateUpdated timestamp,
	primary key (trackid, deviceid)
);

create table plays
(
	id       serial    not null,
	songid   int       not null references track (id),
	deviceid int       not null references device (id),
	playdate timestamp not null,
	imported boolean   not null default false,
	primary key (songid, deviceid, playdate)
);

create table playcount
(
	songid    int     not null references track (id),
	deviceid  int     not null references device (id),
	playcount int     not null,
	imported  boolean not null default true,
	primary key (songid, deviceid)
);