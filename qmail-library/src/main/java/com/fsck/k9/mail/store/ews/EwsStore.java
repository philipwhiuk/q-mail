package com.fsck.k9.mail.store.ews;

import com.fsck.k9.mail.ConnectionSecurity;
import com.fsck.k9.mail.K9MailLib;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.ServerSettings;
import com.fsck.k9.mail.ssl.TrustedSocketFactory;
import com.fsck.k9.mail.store.RemoteStore;
import com.fsck.k9.mail.store.StoreConfig;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.exception.service.remote.ServiceResponseException;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
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

import android.support.annotation.NonNull;


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

    @NonNull
    @Override
    public List<EwsFolder> getFolders(boolean forceListAll) throws MessagingException {
        folderCache.clear();
        boolean foundInbox = false;
        try {
            FindFoldersResults results =
                    service.findFolders(WellKnownFolderName.MsgFolderRoot, new FolderView(Integer.MAX_VALUE));
            for (microsoft.exchange.webservices.data.core.service.folder.Folder folder: results.getFolders()) {
                folderCache.put(folder.getId().getUniqueId(), new EwsFolder(this, folder));
            }
            Folder inbox = Folder.bind(service, WellKnownFolderName.Inbox);
            mStoreConfig.setInboxFolderId(inbox.getId().getUniqueId());
            mStoreConfig.setAutoExpandFolderId(inbox.getId().getUniqueId());
            try {
                Folder drafts = Folder.bind(service, WellKnownFolderName.Drafts);
                mStoreConfig.setDraftsFolderId(drafts.getId().getUniqueId());
            } catch (ServiceResponseException e) {
                mStoreConfig.setDraftsFolderId(K9MailLib.FOLDER_NONE);
            }
            try {
                Folder sent = Folder.bind(service, WellKnownFolderName.SentItems);
                mStoreConfig.setSentFolderId(sent.getId().getUniqueId());
            } catch (ServiceResponseException e) {
                mStoreConfig.setSentFolderId(K9MailLib.FOLDER_NONE);
            }
            try {
                Folder deleted = Folder.bind(service, WellKnownFolderName.DeletedItems);
                mStoreConfig.setTrashFolderId(deleted.getId().getUniqueId());
            } catch (ServiceResponseException e) {
                mStoreConfig.setTrashFolderId(K9MailLib.FOLDER_NONE);
            }
            try {
                Folder junk = Folder.bind(service, WellKnownFolderName.JunkEmail);
                mStoreConfig.setSpamFolderId(junk.getId().getUniqueId());
            } catch (ServiceResponseException e) {
                mStoreConfig.setSpamFolderId(K9MailLib.FOLDER_NONE);
            }
            try {
                Folder archive = Folder.bind(service, WellKnownFolderName.ArchiveMsgFolderRoot);
                mStoreConfig.setArchiveFolderId(archive.getId().getUniqueId());
            } catch (ServiceResponseException e) {
                mStoreConfig.setArchiveFolderId(K9MailLib.FOLDER_NONE);
            }
        } catch (Exception e) {
            throw new MessagingException("Unable to find folders", e);
        }
        ArrayList<EwsFolder> folders = new ArrayList<>();
        folders.addAll(folderCache.values());
        return folders;
    }

    @Override
    @NonNull
    public List<EwsFolder> getSubFolders(final String parentFolderId, boolean forceListAll) throws MessagingException {
        List<EwsFolder> folders = getFolders(forceListAll);
        List<EwsFolder> subFolders = new ArrayList<>();

        for (EwsFolder folder: folders) {
            if (folder.getId().startsWith(parentFolderId) && folder.getId().length() != parentFolderId.length()) {
                subFolders.add(folder);
            }
        }

        return subFolders;
    }

    @Override
    public void checkSettings() throws MessagingException {
        try {
            getFolders(true);
        } catch (Exception e) {
            Throwable cause = e;
            while (cause.getCause() != null && cause.getCause().getMessage() != null) {
                cause = cause.getCause();
            }
            throw new MessagingException(cause.getMessage(), e);
        }
    }

    public FolderId getRootFolderId() {
        return rootFolderId;
    }
}
