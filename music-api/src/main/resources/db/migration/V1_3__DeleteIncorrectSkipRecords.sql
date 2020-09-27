-- The UI was incorrectly recording songs played to the end as skips. This script deletes skip records that are actually
-- full plays of the song.

delete from skips where id in (
	select s.id from skips s
						 inner join track t on s.songid = t.id
	where (t.duration - s.secondsplayed) < 1
)