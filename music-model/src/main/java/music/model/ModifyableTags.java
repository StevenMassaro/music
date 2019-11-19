package music.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jaudiotagger.tag.FieldKey;

import java.sql.JDBCType;
import java.sql.SQLType;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ModifyableTags {
    TITLE(FieldKey.TITLE, HtmlType.text, JDBCType.VARCHAR),
    ALBUM(FieldKey.ALBUM, HtmlType.text, JDBCType.VARCHAR),
	ARTIST( FieldKey.ARTIST, HtmlType.text, JDBCType.VARCHAR),
	ALBUM_ARTIST( FieldKey.ALBUM_ARTIST, HtmlType.text, JDBCType.VARCHAR),
	GENRE( FieldKey.GENRE, HtmlType.text, JDBCType.VARCHAR),
	YEAR( FieldKey.YEAR, HtmlType.text, JDBCType.VARCHAR), //todo should this be text?
	DISC_NO(FieldKey.DISC_NO, HtmlType.number, JDBCType.INTEGER),
	TRACK(FieldKey.TRACK, HtmlType.number, JDBCType.INTEGER),
//	RATING???
	COMMENT(FieldKey.COMMENT, HtmlType.text, JDBCType.VARCHAR);

    private FieldKey associatedKey;
    private HtmlType htmlType;
    private SQLType sqlType;

    ModifyableTags(FieldKey fieldKey, HtmlType htmlType, SQLType sqlType) {
        this.associatedKey = fieldKey;
        this.htmlType = htmlType;
        this.sqlType = sqlType;
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
}
