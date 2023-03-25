package ro.niconeko.astralbooks.persistent;

import org.bukkit.persistence.PersistentDataType;

@SuppressWarnings("rawtypes")
public enum NBTDataType {
    BYTE(PersistentDataType.BYTE),
    BYTE_ARRAY(PersistentDataType.BYTE_ARRAY),
    INTEGER(PersistentDataType.INTEGER),
    INTEGER_ARRAY(PersistentDataType.INTEGER_ARRAY),
    LONG(PersistentDataType.LONG),
    LONG_ARRAY(PersistentDataType.LONG_ARRAY),
    DOUBLE(PersistentDataType.DOUBLE),
    FLOAT(PersistentDataType.FLOAT),
    SHORT(PersistentDataType.SHORT),
    TAG_CONTAINER(PersistentDataType.TAG_CONTAINER),
    TAG_CONTAINER_ARRAY(PersistentDataType.TAG_CONTAINER_ARRAY),
    STRING(PersistentDataType.STRING);

    final PersistentDataType type;

    NBTDataType(PersistentDataType type) {
        this.type = type;
    }

    public PersistentDataType getType() {
        return this.type;
    }
}
