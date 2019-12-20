alter table music.track add unique(id);

alter table music.plays add foreign key (songid) references music.track(id);

ALTER TABLE music.plays
ADD COLUMN deviceid int,
ADD FOREIGN KEY (deviceid) REFERENCES music.device(id);

update plays set deviceid = 1;

alter table music.plays alter column deviceid set not null;

alter table music.plays add primary key (songid, deviceid, playdate);

alter table music.plays add column imported boolean default false;

alter table music.track add column rating smallint constraint within_range check ((rating > 0 and rating < 10) or rating = null)

ALTER TABLE music.track RENAME COLUMN albumArtist TO album_artist;
ALTER TABLE music.track RENAME COLUMN discnumber TO disc_no;
ALTER TABLE music.track RENAME COLUMN tracknumber TO track;

alter table music.track add column bitrate int not null;
alter table music.track add column encoding varchar;
alter table music.track add column sampleRate int not null;
alter table music.track add column duration int not null;

alter table music.device add column format varchar;
alter table music.device add column bitrate int;
alter table music.device add column sampleRate int;
alter table music.device add column channels int;

alter table music.device add column artsize int;