package music.model;

import org.jaudiotagger.tag.FieldKey;

public enum ModifyableTags {
    TITLE("title", FieldKey.TITLE),
    ALBUM("album", FieldKey.ALBUM);

    private String propertyName;
    private FieldKey associatedKey;

    ModifyableTags(String propertyName, FieldKey fieldKey) {
        this.propertyName = propertyName;
        this.associatedKey = fieldKey;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public FieldKey getAssociatedKey() {
        return associatedKey;
    }

    public void setAssociatedKey(FieldKey associatedKey) {
        this.associatedKey = associatedKey;
    }
}
