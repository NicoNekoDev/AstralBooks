package ro.niconeko.astralbooks.storage;

public enum StorageType {
    JSON("json"), MYSQL("mysql"), SQLITE("sqlite");

    private final String type;

    StorageType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }

    public static StorageType fromString(String side) {
        if (side.equalsIgnoreCase("mysql"))
            return MYSQL;
        else if (side.equalsIgnoreCase("sqlite"))
            return SQLITE;
        return JSON;
    }
}
