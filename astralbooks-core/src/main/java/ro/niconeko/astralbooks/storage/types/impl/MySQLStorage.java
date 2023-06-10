package ro.niconeko.astralbooks.storage.types.impl;

import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.storage.StorageType;
import ro.niconeko.astralbooks.storage.types.RemoteStorage;

public class MySQLStorage extends RemoteStorage {

    public MySQLStorage(AstralBooksPlugin plugin) {
        super(plugin, StorageType.MYSQL);
    }

    @Override
    protected String getDriver() {
        return "com.mysql.jdbc.Driver";
    }

    @Override
    protected String getURL() {
        return "jdbc:mysql://"
                + super.host
                + ":" + super.port
                + "/" + super.database
                + "?user=" + super.user
                + "&password=" + super.pass
                + "&useSSL=" + super.sslEnabled
                + "&autoReconnect=true";
    }
}
