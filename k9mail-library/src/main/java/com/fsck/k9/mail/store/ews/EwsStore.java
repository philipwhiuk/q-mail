package com.fsck.k9.mail.store.ews;

import com.fsck.k9.mail.ConnectionSecurity;
import com.fsck.k9.mail.Folder;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.ServerSettings;
import com.fsck.k9.mail.ssl.TrustedSocketFactory;
import com.fsck.k9.mail.store.RemoteStore;
import com.fsck.k9.mail.store.StoreConfig;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.search.FindFoldersResults;
import microsoft.exchange.webservices.data.search.FolderView;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EwsStore extends RemoteStore {
    private final ExchangeService service;
    private FolderId rootFolderId;

    public static EwsStoreSettings decodeUri(String uri) {
        return EwsStoreUriDecoder.decode(uri);
    }

    public static String createUri(ServerSettings server) {
        return EwsStoreUriCreator.create(server);
    }

    private String endpoint;
    private final Map<String, EwsFolder> folderCache = new HashMap<String, EwsFolder>();

    public EwsStore(StoreConfig storeConfig, TrustedSocketFactory trustedSocketFactory) throws MessagingException {
        super(storeConfig, trustedSocketFactory);

        EwsStoreSettings settings;
        try {
            settings = decodeUri(storeConfig.getStoreUri());
        } catch (IllegalArgumentException e) {
            throw new MessagingException("Error while decoding store URI", e);
        }

        endpoint = buildEndpoint(settings.host, settings.port, settings.connectionSecurity, settings.path);

        service = new ExchangeService(settings.exchangeVersion);
        ExchangeCredentials credentials = new WebCredentials(settings.username, settings.password);
        service.setCredentials(credentials);
        try {
            service.setUrl(new URI(endpoint));
        } catch (URISyntaxException e) {
            throw new MessagingException("Invalid URI: " + endpoint, e);
        }
    }

    public ExchangeService getService() {
        return service;
    }

    private String buildEndpoint(String host, int port, ConnectionSecurity connectionSecurity, String path) {
        switch (connectionSecurity) {
            case NONE:
                return "http://"+host+":"+port+"/"+path;
            case SSL_TLS_REQUIRED:
                return "https://"+host+":"+port+"/"+path;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public EwsFolder getFolder(String folderId) {
        EwsFolder folder;
        synchronized (folderCache) {
            folder = folderCache.get(folderId);
            if (folder == null) {
                folder = new EwsFolder(this, folderId);
                folderCache.put(folderId, folder);
            }
        }

        return folder;
    }

    private String getFolderIdFromName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends Folder> getPersonalNamespaces(boolean forceListAll) throws MessagingException {
        folderCache.clear();
        try {
            FindFoldersResults results =
                    service.findFolders(WellKnownFolderName.MsgFolderRoot, new FolderView(Integer.MAX_VALUE));
            for (microsoft.exchange.webservices.data.core.service.folder.Folder folder: results.getFolders()) {
                folderCache.put(folder.getId().getUniqueId(), new EwsFolder(this, folder));
            }

        } catch (Exception e) {
            throw new MessagingException("Unable to find folders", e);
        }
        ArrayList<EwsFolder> folders = new ArrayList<>();
        folders.addAll(folderCache.values());
        return folders;
    }

    @Override
    public void checkSettings() throws MessagingException {
        try {
            service.findFolders(WellKnownFolderName.MsgFolderRoot, new FolderView(1));
        } catch (Exception e) {
            throw new MessagingException(e.getMessage(), e);
        }
    }

    public FolderId getRootFolderId() {
        return rootFolderId;
    }
}
