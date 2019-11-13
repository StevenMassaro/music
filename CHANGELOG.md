# changelog

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
