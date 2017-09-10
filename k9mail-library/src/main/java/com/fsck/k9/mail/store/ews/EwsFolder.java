package com.fsck.k9.mail.store.ews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.support.annotation.Nullable;

import com.fsck.k9.mail.FetchProfile;
import com.fsck.k9.mail.Flag;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessageRetrievalListener;
import com.fsck.k9.mail.MessagingException;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.search.FolderTraversal;
import microsoft.exchange.webservices.data.core.enumeration.search.SortDirection;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.FolderView;
import microsoft.exchange.webservices.data.search.ItemView;
import timber.log.Timber;


class EwsFolder extends com.fsck.k9.mail.Folder<Message> {

    private FolderId folderId;
    private EwsStore store;
    private String displayName;
    private int messageCount;
    private Folder folder;

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
        try {
            folder = Folder.bind(store.getService(), folderId);
            folder.getUnreadCount();
            messageCount = folder.getTotalCount();
        } catch (Exception e) {
            throw new MessagingException("Unable to open folder", e);
        }
    }

    @Override
    public void close() {
        folder = null;
    }

    @Override
    public boolean isOpen() {
        return folder != null;
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
            return folder.getUnreadCount();
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
    public List<Message> getMessages(int start, int end, Date earliestDate, @Nullable MessageRetrievalListener<Message> listener)
            throws MessagingException {
        ArrayList<Message> messages = new ArrayList<>();
        try {
            ItemView view = new ItemView(end - start, start);
            view.getOrderBy().add(ItemSchema.DateTimeReceived, SortDirection.Descending);
            view.setPropertySet(new PropertySet(BasePropertySet.IdOnly));

            FindItemsResults<Item> results = folder.findItems(view);
            List<Item> items = results.getItems();
            int count = items.size();
            for (int i = 0; i < items.size(); i++) {
                String uid = items.get(i).getId().getUniqueId();
                if (listener != null)
                    listener.messageStarted(uid, i, count);
                EwsMessage message = new EwsMessage(uid, this);
                messages.add(message);
                if (listener != null)
                    listener.messageFinished(message, i, count);
            }
        } catch (Exception e) {
            throw new MessagingException("Failed to fetch messages", e);
        }
        return messages;
    }

    @Override
    public boolean areMoreMessagesAvailable(int indexOfOldestMessage, Date earliestDate)
            throws IOException, MessagingException {
        try {
            ItemView view = new ItemView(1, indexOfOldestMessage);
            return folder.findItems(view).isMoreAvailable();
        } catch (Exception e) {
            throw new MessagingException("Failed to check for more messages", e);
        }
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
        try {
            for (Message message : messages) {
                EmailMessage email = EmailMessage.bind(store.getService(), new ItemId(message.getUid()));
                //TODO: FETCH Profiles
            }
        } catch (Exception e) {
            throw new MessagingException("Failed to fetch messages", e);
        }
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
