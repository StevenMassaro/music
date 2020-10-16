# changelog

## 1.13.03
* fix "replacing a track with another track doesn't work properly if the new track has the same exact filename as the old track"

## 1.13.02-ALPINE
* switch docker image to use alpine base (which is smaller)

## 1.13.01-SKIP-COUNT
* return skip count as part of track metadata

## 1.13-SKIP-COUNT
* record each time a song is skipped

## 1.12-2020.06.09
* allow song upload to replace existing song and take over its play history

## 1.11-2020.01.27
* refactor music-utils to a separate module

## 1.11-2020.01.14
* fix: when updating album art for entire album, ensure that album from same artist and disc number are retrieved from database
* fix: delete queued track updates when purging tracks

## 1.11-2020.01.12
* feature: support uploading new tracks through `/track/upload` endpoint

## 1.10-2020.01.07
* feature: websocket for album art update status

## 1.9-2020.01.04
* feature: change album art

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
