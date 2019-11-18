package music.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jaudiotagger.tag.FieldKey;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ModifyableTags {
    TITLE(FieldKey.TITLE, HtmlType.text),
    ALBUM(FieldKey.ALBUM, HtmlType.text),
	ARTIST( FieldKey.ARTIST, HtmlType.text),
	ALBUM_ARTIST( FieldKey.ALBUM_ARTIST, HtmlType.text),
	GENRE( FieldKey.GENRE, HtmlType.text),
	YEAR( FieldKey.YEAR, HtmlType.text), //todo should this be text?
	DISC_NO(FieldKey.DISC_NO, HtmlType.number),
	TRACK(FieldKey.TRACK, HtmlType.number),
//	RATING???
	COMMENT(FieldKey.COMMENT, HtmlType.text);

    private FieldKey associatedKey;
    private HtmlType htmlType;

    ModifyableTags(FieldKey fieldKey, HtmlType htmlType) {
        this.associatedKey = fieldKey;
        this.htmlType = htmlType;
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
}
