alter table music.track add unique(id);

alter table music.plays add foreign key (songid) references music.track(id);

ALTER TABLE music.plays
ADD COLUMN deviceid int,
ADD FOREIGN KEY (deviceid) REFERENCES music.device(id);

update plays set deviceid = 1;

alter table music.plays alter column deviceid set not null;

alter table music.plays add primary key (songid, deviceid, playdate);