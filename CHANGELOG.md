# changelog

## 1.8-2019.12.24
* put back music-service module

## 1.8-2019.12.20
* feature: scale album art when syncing to devices (apparently, Android won't correctly grab metadata from tracks if a song's embedded album art exceeds some unknown criteria (perhaps dimensions or overall size))
* build docker image as separate maven module

## 1.8-2019.12.09
* feature: services necessary to support converting files
* remove music-service module

## 1.7-2019.12.02
* feature: edit rating for a song

## 1.6-2019.12.02
* feature: smart playlists

## 1.5-2019.11.19
* feature: persist metadata changes for a track to disk

## 1.4-2019.11.18
* feature: edit metadata for a track
* fix: cannot change a track field if the current value is null

## 1.3-2019.11.15
* feature: add endpoint for count of purgeable tracks (tracks marked deleted in database)

## 1.2-2019.11.14
* feature: show historical plays (songs played on a particular date)

## 1.2-2019.11.12
* fix: prevent sync from beginning if sync is already occurring
* fix: prevent migration from beginning if migration is already occurring

## 1.2-2019.11.08
* move services to separate module for sharing to client

## 1.2-2019.11.07-SNAPSHOT
* feature: retrieve album art

## 1.1-2019.10.30-SNAPSHOT
* feature: allow updates to be forced during sync (though request param `forceUpdates`)
* fix: `updateByLocation` correctly gets bitrate from POJO
* fix: `updateByLocation` sets `dateUpdated` in database

## 1.1-2019.10.29-SNAPSHOT
* retrieve audio file header information (like file length)
