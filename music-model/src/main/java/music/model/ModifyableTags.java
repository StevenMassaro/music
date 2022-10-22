package music.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jaudiotagger.tag.FieldKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.JDBCType;
import java.sql.SQLType;
import java.util.function.BiFunction;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ModifyableTags {
	TITLE(FieldKey.TITLE, HtmlType.text, JDBCType.VARCHAR, (track, newValue) -> {
		track.setTitle(newValue);
		return null;
	}),
	ALBUM(FieldKey.ALBUM, HtmlType.text, JDBCType.VARCHAR, (track, newValue) -> {
		track.setAlbum(newValue);
		return null;
	}),
	ARTIST(FieldKey.ARTIST, HtmlType.text, JDBCType.VARCHAR, (track, newValue) -> {
		track.setArtist(newValue);
		return null;
	}),
	ALBUM_ARTIST(FieldKey.ALBUM_ARTIST, HtmlType.text, JDBCType.VARCHAR, (track, newValue) -> {
		track.setAlbum_artist(newValue);
		return null;
	}),
	GENRE(FieldKey.GENRE, HtmlType.text, JDBCType.VARCHAR, (track, newValue) -> {
		track.setGenre(newValue);
		return null;
	}),
	YEAR(FieldKey.YEAR, HtmlType.text, JDBCType.VARCHAR, (track, newValue) -> {
		track.setYear(newValue);
		return null;
	}), //todo should this be text?
	DISC_NO(FieldKey.DISC_NO, HtmlType.number, JDBCType.INTEGER, (track, newValue) -> {
		track.setDisc_no(Long.valueOf(newValue));
		return null;
	}),
	TRACK(FieldKey.TRACK, HtmlType.number, JDBCType.INTEGER, (track, newValue) -> {
		track.setTrack(Long.valueOf(newValue));
		return null;
	}),
	//	RATING???
	COMMENT(FieldKey.COMMENT, HtmlType.text, JDBCType.VARCHAR, (track, newValue) -> {
		track.setComment(newValue);
		return null;
	});

	private FieldKey associatedKey;
	private HtmlType htmlType;
	private SQLType sqlType;
	private BiFunction<Track, String, Void> updateModel;

	ModifyableTags(FieldKey fieldKey, HtmlType htmlType, SQLType sqlType, BiFunction<Track, String, Void> updateModel) {
		this.associatedKey = fieldKey;
		this.htmlType = htmlType;
		this.sqlType = sqlType;
		this.updateModel = updateModel;
	}

    public String getPropertyName() {
        return this.name().toLowerCase();
    }

    @JsonIgnore
    public FieldKey getAssociatedKey() {
        return associatedKey;
    }

    public void setAssociatedKey(FieldKey associatedKey) {
        this.associatedKey = associatedKey;
    }

	public HtmlType getHtmlType() {
		return htmlType;
	}

	public void setHtmlType(HtmlType htmlType) {
		this.htmlType = htmlType;
	}

	public SQLType getSqlType() {
		return sqlType;
	}

	public void setSqlType(SQLType sqlType) {
		this.sqlType = sqlType;
	}

	public void updateModel(@Nullable Track track, @NotNull String newValue) {
		this.updateModel.apply(track, newValue);
	}
}
