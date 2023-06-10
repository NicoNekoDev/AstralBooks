package ro.niconeko.astralbooks.storage;

public enum StorageType {
    JSON("json", "Json"),
    MYSQL("mysql", "MySQL"),
    SQLITE("sqlite", "SQLite"),
    H2("h2", "H2"),
    MARIADB("mariadb", "MariaDB");

    private final String type;
    private final String formattedName;

    StorageType(String type, String formattedName) {
        this.type = type;
        this.formattedName = formattedName;
    }

    @Override
    public String toString() {
        return this.type;
    }

    public String getFormattedName() {
        return formattedName;
    }

    public static StorageType fromString(String type) {
        if (type.equalsIgnoreCase("mysql"))
            return MYSQL;
        else if (type.equalsIgnoreCase("sqlite"))
            return SQLITE;
        else if (type.equalsIgnoreCase("json"))
            return JSON;
        else if (type.equalsIgnoreCase("mariadb"))
            return MARIADB;
        return H2;
    }
}
