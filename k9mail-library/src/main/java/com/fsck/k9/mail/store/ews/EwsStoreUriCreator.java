package com.fsck.k9.mail.store.ews;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import com.fsck.k9.mail.AuthType;
import com.fsck.k9.mail.ServerSettings;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;

import static com.fsck.k9.mail.helper.UrlEncodingHelper.encodeUtf8;


public class EwsStoreUriCreator {
    /**
     * Creates an ImapStore URI with the supplied settings.
     *
     * @param server
     *         The {@link ServerSettings} object that holds the server settings.
     *
     * @return An ImapStore URI that holds the same information as the {@code server} parameter.
     *
     * @see com.fsck.k9.mail.store.StoreConfig#getStoreUri()
     * @see EwsStore#decodeUri(String)
     */
    public static String create(ServerSettings server) {
        String userEnc = encodeUtf8(server.username);
        String passwordEnc = (server.password != null) ? encodeUtf8(server.password) : "";
        String clientCertificateAliasEnc = (server.clientCertificateAlias != null) ?
                encodeUtf8(server.clientCertificateAlias) : "";

        String scheme;
        switch (server.connectionSecurity) {
            case SSL_TLS_REQUIRED:
                scheme = "ews+ssl+";
                break;
            case STARTTLS_REQUIRED:
                scheme = "ews+tls+";
                break;
            default:
            case NONE:
                scheme = "ews";
                break;
        }

        AuthType authType = server.authenticationType;
        String userInfo;
        if (authType == AuthType.EXTERNAL) {
            userInfo = authType.name() + ":" + userEnc + ":" + clientCertificateAliasEnc;
        } else {
            userInfo = authType.name() + ":" + userEnc + ":" + passwordEnc;
        }
        try {
            Map<String, String> extra = server.getExtra();
            String path;
            String exchangeVersion;
            if (extra != null) {
                exchangeVersion = extra.get(EwsStoreSettings.EXCHANGE_VERSION_KEY);
                path = "/"+exchangeVersion+"/"+extra.get(EwsStoreSettings.PATH_KEY);
            } else {
                exchangeVersion = ExchangeVersion.Exchange2010_SP2.name();
                path = "/"+exchangeVersion+"/";
            }
            return new URI(scheme, userInfo, server.host, server.port, path, null, null).toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Can't create EwsStore URI", e);
        }
    }
}
