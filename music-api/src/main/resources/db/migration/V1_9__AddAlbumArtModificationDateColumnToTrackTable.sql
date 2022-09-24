alter table track add column albumArtDateUpdated timestamptz;
update track set albumArtDateUpdated = now() where albumartsource is not null;