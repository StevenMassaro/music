create database music;
-- pause and set active

create schema music;

drop table if exists music.track;
create table music.track (
	id serial not null,
  title varchar(1000) not null,
  album varchar(1000),
  artist varchar(1000),
  albumArtist varchar(1000),
  genre varchar(1000),
  year varchar(1000),
	dateCreated timestamp DEFAULT now()
);