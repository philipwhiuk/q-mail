package com.fsck.k9.mailstore;


import java.util.Collections;
import java.util.List;

import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.Part;
import com.fsck.k9.mail.internet.DKIMState;
import com.fsck.k9.mail.internet.ReceivedHeaders;
import com.fsck.k9.mail.internet.SPFState;
import com.fsck.k9.mail.internet.SecureTransportState;


public class MessageViewInfo {
    public final Message message;
    public final boolean isMessageIncomplete;
    public final Part rootPart;
    public final AttachmentResolver attachmentResolver;
    public final String text;
    public final CryptoResultAnnotation cryptoResultAnnotation;
    public final SecureTransportState secureTransportState;
    public final SPFState spfState;
    public final DKIMState dkimState;
    public final List<AttachmentViewInfo> attachments;
    public final String extraText;
    public final List<AttachmentViewInfo> extraAttachments;
    public final List<ICalendarViewInfo> iCalendarEvents;
    public final List<ICalendarViewInfo> extraICalendars;

    public MessageViewInfo(
            Message message, boolean isMessageIncomplete, Part rootPart,
            String text, List<AttachmentViewInfo> attachments,
            List<ICalendarViewInfo> iCalendarEvents,
            CryptoResultAnnotation cryptoResultAnnotation,
            AttachmentResolver attachmentResolver,
            String extraText, List<AttachmentViewInfo> extraAttachments, List<ICalendarViewInfo> extraICalendars) {
        this.message = message;
        this.isMessageIncomplete = isMessageIncomplete;
        this.rootPart = rootPart;
        this.text = text;
        this.cryptoResultAnnotation = cryptoResultAnnotation;
        this.attachmentResolver = attachmentResolver;
        this.attachments = attachments;
        this.iCalendarEvents = iCalendarEvents;
        this.extraText = extraText;
        this.extraAttachments = extraAttachments;
        this.extraICalendars = extraICalendars;
        secureTransportState = ReceivedHeaders.wasMessageTransmittedSecurely(message);
        spfState = ReceivedHeaders.isEmailPotentialSpoof(message);
        dkimState = ReceivedHeaders.isEmailIntegrityValid(message);
    }

    static MessageViewInfo createWithExtractedContent(
            Message message, boolean isMessageIncomplete,
            String text, List<AttachmentViewInfo> attachments,
            List<ICalendarViewInfo> iCalendarEvents,
            AttachmentResolver attachmentResolver) {
        return new MessageViewInfo(
                message, isMessageIncomplete, message,
                text, attachments, iCalendarEvents,
                null,
                attachmentResolver,
                null,
                Collections.<AttachmentViewInfo>emptyList(),
                Collections.<ICalendarViewInfo>emptyList()
        );
    }

    public static MessageViewInfo createWithErrorState(Message message, boolean isMessageIncomplete) {
        return new MessageViewInfo(message, isMessageIncomplete, null, null,
                null, null, null,
                null, null, null, null);
    }

    MessageViewInfo withCryptoData(CryptoResultAnnotation rootPartAnnotation, String extraViewableText,
            List<AttachmentViewInfo> extraAttachmentInfo, List<ICalendarViewInfo> extraCalendarInfos) {
        return new MessageViewInfo(
                message, isMessageIncomplete, rootPart, text, attachments,
                iCalendarEvents,
                rootPartAnnotation,
                attachmentResolver,
                extraViewableText, extraAttachmentInfo, extraCalendarInfos
        );
    }
}
