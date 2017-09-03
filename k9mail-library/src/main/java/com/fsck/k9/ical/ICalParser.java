package com.fsck.k9.ical;

import com.fsck.k9.mail.internet.MessageExtractor;

import biweekly.Biweekly;

public class ICalParser {
    public static final String MIME_TYPE = "text/calendar";

    public static ICalData parse(ICalPart part) {

        String iCalText = MessageExtractor.getTextFromPart(part.getPart());

        return new ICalData(Biweekly.parse(iCalText).all());

    }
}
