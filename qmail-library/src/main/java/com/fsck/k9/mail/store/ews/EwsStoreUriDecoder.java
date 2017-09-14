package com.fsck.k9.mail.store.ews;


import java.net.URI;
import java.net.URISyntaxException;

import com.fsck.k9.mail.AuthType;
import com.fsck.k9.mail.ConnectionSecurity;
import com.fsck.k9.mail.ServerSettings;
import com.fsck.k9.mail.ServerSettings.Type;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;

import static com.fsck.k9.mail.helper.UrlEncodingHelper.decodeUtf8;


class EwsStoreUriDecoder {
    /**
     * Decodes an EwsStore URI.
     *
     * <p>Possible forms:</p>
     * <pre>
     * ews://auth:user:password@server:port/ ConnectionSecurity.NONE
     * ews+ssl+://auth:user:password@server:port ConnectionSecurity.SSL_TLS_REQUIRED
     * </pre>
     *
     * Example encoded URL
     *
     * ews+ssl+://abad@outlook.com:fuzzyBoots:outlook.office365.com:443/Exchange2007_SP1/EWS/Exchange.asmx
     *
     * Resulting Ews Store details
     * Endpoint: https://outlook.office365.com/EWS/Exchange.asmx
     * User: abad@outlook.com
     * Password: fuzzyBoots
     * {@link ExchangeVersion}: {@code ExchangeVersion.Exchange2007_SP1}
     *
     * @param uri the store uri.
     */
    public static EwsStoreSettings decode(String uri) {

        URI ewsUri;
        ConnectionSecurity connectionSecurity;
        String host;
        int port;
        String username = null;
        String password = null;
        String path;
        String clientCertificateAlias = null;
        AuthType authenticationType = null;
        ExchangeVersion exchangeVersion;

        try {
            ewsUri = new URI(uri);
        } catch (URISyntaxException use) {
            throw new IllegalArgumentException("Invalid EWS URI", use);
        }

        String scheme = ewsUri.getScheme();
        if (scheme.equals("ews")) {
            connectionSecurity = ConnectionSecurity.NONE;
            port = Type.EWS.defaultPort;
        } else if (scheme.startsWith("ews+ssl")) {
            connectionSecurity = ConnectionSecurity.SSL_TLS_REQUIRED;
            port = Type.EWS.defaultTlsPort;
        } else {
            throw new IllegalArgumentException("Unsupported protocol (" + scheme + ")");
        }

        host = ewsUri.getHost();

        if (ewsUri.getPort() != -1) {
            port = ewsUri.getPort();
        }

        if (ewsUri.getUserInfo() != null) {
            String userinfo = ewsUri.getUserInfo();
            String[] userInfoParts = userinfo.split(":");

            if (userinfo.endsWith(":")) {
                // Last field (password/certAlias) is empty.
                // For imports e.g.: PLAIN:username: or username:
                // Or XOAUTH2 where it's a valid config - XOAUTH:username:
                if (userInfoParts.length > 1) {
                    authenticationType = AuthType.valueOf(userInfoParts[0]);
                    username = decodeUtf8(userInfoParts[1]);
                } else {
                    authenticationType = AuthType.PLAIN;
                    username = decodeUtf8(userInfoParts[0]);
                }
            } else if (userInfoParts.length == 2) {
                // Old/standard style of encoding - PLAIN auth only:
                // username:password
                authenticationType = AuthType.PLAIN;
                username = decodeUtf8(userInfoParts[0]);
                password = decodeUtf8(userInfoParts[1]);
            } else if (userInfoParts.length == 3) {
                // Standard encoding
                // PLAIN:username:password
                // EXTERNAL:username:certAlias
                authenticationType = AuthType.valueOf(userInfoParts[0]);
                username = decodeUtf8(userInfoParts[1]);

                if (AuthType.EXTERNAL == authenticationType) {
                    clientCertificateAlias = decodeUtf8(userInfoParts[2]);
                } else {
                    password = decodeUtf8(userInfoParts[2]);
                }
            }
        }

        String[] encodedParts = ewsUri.getPath().split("/", 3);
        if (encodedParts.length >= 2) {
            path = encodedParts[encodedParts.length - 1];
        } else {
            path = "";
        }
        if (encodedParts.length >= 3) {
            exchangeVersion = ExchangeVersion.valueOf(encodedParts[1]);
        } else {
            exchangeVersion = ExchangeVersion.Exchange2010_SP2;
        }

        return new EwsStoreSettings(host, port, path, connectionSecurity, authenticationType,
                username, password, null, exchangeVersion);

    }
}
