alter table library add column trackNamePattern varchar(255);
update library set trackNamePattern = '/ARTIST/ALBUM/TRACK - TITLE';
alter table library alter column trackNamePattern set not null;