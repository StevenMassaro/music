package music.repository

import music.model.Track
import org.springframework.data.repository.CrudRepository

@Deprecated("I deprecated this, because using this in conjunction with the MyBatis mapper causes caching issues. " +
	"Both MyBatis and Hibernate have their own first level caches, and neither one knows when the other should invalidate " +
	"its cache for legitimate reasons (like doing an update). Additionally, the logic to load values that have been updated, " +
	"using the coalesce's in TrackMapper.xml, as well as the calculation for plays and skips and other transitive values " +
	"cannot be done easily in Hibernate. As a result, Track is probably never a model that can be processed in Hibernate, " +
	"but I'm leaving this around as a reminder for the failed experiment.")
interface ITrackRepository: CrudRepository<Track, Long>