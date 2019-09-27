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
  albumArtist varchar(1000),
  genre varchar(1000),
  year varchar(1000),
  discnumber int,
  tracknumber int,
  comment varchar,
  deletedInd boolean not null,
  dateCreated timestamp DEFAULT now(),
  dateUpdated timestamp,
  fileLastModifiedDate timestamp
);

drop table if exists music.device cascade;
create table music.device (
  id serial primary key,
  name varchar not null unique,
  dateCreated timestamp DEFAULT now()
);

drop table if exists music.plays cascade;
create table music.plays (
  id serial not null,
  songid int not null references music.track(id),
  deviceid int not null references music.device(id),
  playdate timestamp not null
);