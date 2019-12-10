-- create database music;
-- pause and set active

create schema music;

drop table if exists music.track cascade;
create table music.track (
  id serial not null unique,
  title varchar(1000) not null,
  location varchar primary key,
  hash varchar(128) not null,
  album varchar(1000),
  artist varchar(1000),
  album_artist varchar(1000),
  genre varchar(1000),
  year varchar(1000),
  disc_no int,
  track int,
  rating smallint constraint within_range check ((rating > 0 and rating < 10) or rating = null),
  comment varchar,
  bitrate int not null,
  encoding varchar,
  sampleRate int not null,
  duration int not null,
  deletedInd boolean not null,
  dateCreated timestamp DEFAULT now(),
  dateUpdated timestamp,
  fileLastModifiedDate timestamp
);

drop table if exists music.smartplaylist cascade;
create table music.smartplaylist (
	id serial unique not null,
	name varchar(1000) unique not null,
	dynamicsql varchar not null,
	dateCreated timestamp DEFAULT now(),
	dateUpdated timestamp
);

drop table if exists music.updatetype cascade;
create table music.updatetype (
  id serial unique not null,
  name varchar primary key
);

insert into music.updatetype (id, name) values (1, 'ID3 tag update');

drop table if exists music.trackupdates cascade;
create table music.trackupdates (
  id serial not null unique,
  songid int not null references music.track(id),
  field varchar not null,
  newvalue varchar not null,
  updatetype int not null references music.updatetype(id)
);

drop table if exists music.device cascade;
create table music.device (
  id serial primary key,
  name varchar not null unique,
  format varchar,
  bitrate int,
  sampleRate int,
  channels int,
  dateCreated timestamp DEFAULT now()
);

drop table if exists music.hash cascade;
create table music.hash (
	trackid int not null references music.track(id),
	deviceid int not null references music.device(id),
	hash varchar not null,
	dateCreated timestamp DEFAULT now(),
	dateUpdated timestamp,
	primary key (trackid, deviceid)
);

drop table if exists music.plays cascade;
create table music.plays (
  id serial not null,
  songid int not null references music.track(id),
  deviceid int not null references music.device(id),
  playdate timestamp not null,
  imported boolean not null default false,
  primary key (songid, deviceid, playdate)
);

drop table if exists music.playcount cascade;
create table music.playcount (
  songid int not null references music.track(id),
  deviceid int not null references music.device(id),
  playcount int not null,
  imported boolean not null default true,
  primary key(songid, deviceid)
);
