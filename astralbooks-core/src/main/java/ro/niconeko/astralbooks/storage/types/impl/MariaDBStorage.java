package ro.niconeko.astralbooks.storage.types.impl;

import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.storage.StorageType;
import ro.niconeko.astralbooks.storage.types.RemoteStorage;

public class MariaDBStorage extends RemoteStorage {
    public MariaDBStorage(AstralBooksPlugin plugin) {
        super(plugin, StorageType.MARIADB);
    }

    @Override
    protected String getDriver() {
        return "org.mariadb.jdbc.Driver";
    }

    @Override
    protected String getURL() {
        return "jdbc:mariadb://"
                + super.host
                + ":" + super.port
                + "/" + super.database
                + "?user=" + super.user
                + "&password=" + super.pass
                + "&useSSL=" + super.sslEnabled
                + "&autoReconnect=true";
    }
}
