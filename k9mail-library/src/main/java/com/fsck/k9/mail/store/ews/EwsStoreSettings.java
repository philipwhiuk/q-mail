package com.fsck.k9.mail.store.ews;

import java.util.HashMap;
import java.util.Map;

import com.fsck.k9.mail.AuthType;
import com.fsck.k9.mail.ConnectionSecurity;
import com.fsck.k9.mail.ServerSettings;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;


public class EwsStoreSettings extends ServerSettings {
    public final ExchangeVersion exchangeVersion;
    public final String path;
    public static final String PATH_KEY = "path";
    public static final String EXCHANGE_VERSION_KEY = "exchangeVersion";

    EwsStoreSettings(String host, int port, String path, ConnectionSecurity connectionSecurity,
                            AuthType authenticationType, String username, String password,
                            String clientCertificateAlias, ExchangeVersion exchangeVersion) {
        super(Type.EWS, host, port, connectionSecurity, authenticationType, username, password, clientCertificateAlias);
        this.exchangeVersion = exchangeVersion;
        this.path = path;
    }

    @Override
    public Map<String, String> getExtra() {
        Map<String, String> extra = new HashMap<>();
        putIfNotNull(extra, PATH_KEY, path);
        putIfNotNull(extra, EXCHANGE_VERSION_KEY, exchangeVersion.name());

        return extra;
    }

    @Override
    public ServerSettings newPassword(String newPassword) {
        return new EwsStoreSettings(host, port, path, connectionSecurity, authenticationType,
                username, newPassword, clientCertificateAlias, exchangeVersion);
    }
}
