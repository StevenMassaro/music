package music.model

import jakarta.persistence.*


@Entity
@Table(name = "trackupdates")
open class TrackUpdate(
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		open var id: Long? = null,
        open var songId: Long,
        open var field: String,
        open var newValue: String,
        open var updateType: Long? = 1
) {
	override fun toString(): String {
		return "TrackUpdate(id=$id, songId=$songId, field='$field', newValue='$newValue', updateType=$updateType)"
	}

	fun getModifyableTag(): ModifyableTags? {
		return ModifyableTags.values().find { mt -> mt.propertyName == field }
	}
}