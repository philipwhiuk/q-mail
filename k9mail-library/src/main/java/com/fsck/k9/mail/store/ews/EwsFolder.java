package com.fsck.k9.mail.store.ews;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fsck.k9.mail.FetchProfile;
import com.fsck.k9.mail.Flag;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessageRetrievalListener;
import com.fsck.k9.mail.MessagingException;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.search.FolderTraversal;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.search.FolderView;

class EwsFolder extends com.fsck.k9.mail.Folder<Message> {

    private FolderId folderId;
    private EwsStore store;
    private String displayName;
    private int messageCount;

    EwsFolder(EwsStore store, Folder folder)
            throws ServiceLocalException {
        this.store = store;
        displayName = folder.getDisplayName();
        folderId = folder.getId();
    }

    EwsFolder(EwsStore store, String uniqueFolderId) {
        this.store = store;
        try {
            this.folderId = new FolderId(uniqueFolderId);
        } catch (Exception e) {
            //Reading the code I don't see how this should happen and it makes store handling very difficult
            throw new RuntimeException("Unable to create folderID with value:" + uniqueFolderId, e);
        }
    }

    @Override
    public void open(int mode) throws MessagingException {
        messageCount = -1;
    }

    @Override
    public void close() {
        folderId = null;
    }

    @Override
    public boolean isOpen() {
        return folderId != null;
    }

    @Override
    public int getMode() {
        return OPEN_MODE_RW;
    }

    @Override
    public boolean create(FolderType type) throws MessagingException {
        try {
            ExchangeService service = store.getService();

            service.createFolder(new Folder(service),
                    store.getRootFolderId());
        } catch (Exception e) {
            throw new MessagingException("Unable to create folder", e);
        }
        return true;
    }

    @Override
    public boolean exists() throws MessagingException {
        return folderId != null || checkFolderExistsRemotely();
    }

    private boolean checkFolderExistsRemotely() throws MessagingException {
        FolderView folderView = new FolderView(Integer.MAX_VALUE);
        folderView.setTraversal(FolderTraversal.Deep);
        try {
            for (Folder f:
                    store.getService().findFolders(store.getRootFolderId(),
                            folderView).getFolders()) {
                if (f.getDisplayName().equals(displayName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new MessagingException("Unable to check folder existence", e);
        }
    }

    @Override
    public int getMessageCount() throws MessagingException {
        return messageCount;
    }

    @Override
    public int getUnreadMessageCount() throws MessagingException {
        try {
            return Folder.bind(store.getService(), folderId).getUnreadCount();
        } catch (Exception e) {
            throw new MessagingException("Unable to get message count", e);
        }
    }

    @Override
    public int getFlaggedMessageCount() throws MessagingException {
        try {
            //TODO: Implement this
            return 0;
        } catch (Exception e) {
            throw new MessagingException("Unable to get message count", e);
        }
    }

    @Override
    public Message getMessage(String uid) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Message> getMessages(int start, int end, Date earliestDate, MessageRetrievalListener<Message> listener)
            throws MessagingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean areMoreMessagesAvailable(int indexOfOldestMessage, Date earliestDate)
            throws IOException, MessagingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> appendMessages(List<? extends Message> messages) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFlags(List<? extends Message> messages, Set<Flag> flags, boolean value) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFlags(Set<Flag> flags, boolean value) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getUidFromMessageId(Message message) throws MessagingException {
        return null;
    }

    @Override
    public void fetch(List<Message> messages, FetchProfile fp, MessageRetrievalListener<Message> listener)
            throws MessagingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(boolean recurse) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return displayName;
    }

    @Override
    public String getId() {
        return folderId.getUniqueId();
    }
}
