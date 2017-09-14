package com.fsck.k9.mail.store.ews;


import com.fsck.k9.mail.AuthType;
import com.fsck.k9.mail.ConnectionSecurity;
import com.fsck.k9.mail.ServerSettings;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class EwsUriTest {

    String host = "outlook.office365.com";
    int port = 443;
    ConnectionSecurity connectionSecurity = ConnectionSecurity.SSL_TLS_REQUIRED;
    AuthType authType = AuthType.AUTOMATIC;
    String username = "ab@cd.com";
    String password = "efgh";
    String clientCert = null;
    String path = "EWS/Exchange.asmx";
    ExchangeVersion exchangeVersion = ExchangeVersion.Exchange2010_SP2;

    @Test
    public void decodingWorksProperly() {
        EwsStoreSettings settings = new EwsStoreSettings(host, port, path, connectionSecurity, authType,
                username, password, clientCert, exchangeVersion);
        String uri = EwsStoreUriCreator.create(settings);
        EwsStoreSettings decodedSettings = EwsStoreUriDecoder.decode(uri);

        assertEquals(settings.host, decodedSettings.host);
        assertEquals(settings.port, decodedSettings.port);
        assertEquals(settings.path, decodedSettings.path);
        assertEquals(settings.connectionSecurity, decodedSettings.connectionSecurity);
        assertEquals(settings.authenticationType, decodedSettings.authenticationType);
        assertEquals(settings.username, decodedSettings.username);
        assertEquals(settings.password, decodedSettings.password);
        assertEquals(settings.exchangeVersion, decodedSettings.exchangeVersion);

    }
}
