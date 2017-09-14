
package com.fsck.k9.mail.transport;


import java.util.Collections;

import com.fsck.k9.mail.K9HttpClient.K9HttpClientFactory;
import com.fsck.k9.mail.K9MailLib;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.Transport;
import com.fsck.k9.mail.ssl.DefaultTrustedSocketFactory;
import com.fsck.k9.mail.store.StoreConfig;
import com.fsck.k9.mail.store.ews.EwsStore;
import com.fsck.k9.mail.store.webdav.WebDavStore;
import timber.log.Timber;


public class EwsTransport extends Transport {
    private EwsStore store;

    public EwsTransport(StoreConfig storeConfig, DefaultTrustedSocketFactory defaultTrustedSocketFactory) throws MessagingException {
        store = new EwsStore(storeConfig, defaultTrustedSocketFactory);

        if (K9MailLib.isDebug())
            Timber.d(">>> New EwsTransport creation complete");
    }

    @Override
    public void open() throws MessagingException {
        if (K9MailLib.isDebug())
            Timber.d( ">>> open called on EwsTransport ");
        store.getService();
    }

    @Override
    public void close() {
    }

    @Override
    public void sendMessage(Message message) throws MessagingException {
        store.sendMessages(Collections.singletonList(message));
    }
}
