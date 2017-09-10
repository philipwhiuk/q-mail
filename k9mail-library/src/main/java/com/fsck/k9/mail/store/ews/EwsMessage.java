package com.fsck.k9.mail.store.ews;

import com.fsck.k9.mail.internet.MimeMessage;

class EwsMessage extends MimeMessage {

    EwsMessage(String uid, EwsFolder folder) {
        this.mUid = uid;
        this.mFolder = folder;
    }
}
