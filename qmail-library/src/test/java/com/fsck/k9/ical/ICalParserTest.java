package com.fsck.k9.ical;


import com.fsck.k9.mail.Body;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.internet.MimeBodyPart;
import com.fsck.k9.mail.internet.TextBody;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ICalParserTest {

    @Test
    public void parse_returnsCorrectDataForMinimalPublishEvent() throws MessagingException {
        String calendar =
"BEGIN:VCALENDAR\r\n" +
"METHOD:PUBLISH\r\n" +
"PRODID:-//ACME/DesktopCalendar//EN\r\n" +
"VERSION:2.0\r\n" +
"BEGIN:VEVENT\r\n" +
"ORGANIZER:mailto:a@example.com\r\n" +
"DTSTART:19970701T200000Z\r\n" +
"DTSTAMP:19970611T190000Z\r\n" +
"SUMMARY:ST. PAUL SAINTS -VS- DULUTH-SUPERIOR DUKES\r\n" +
"UID:0981234-1234234-23@example.com\r\n" +
"END:VEVENT\r\n" +
"END:VCALENDAR";
        Body body = new TextBody(calendar);
        MimeBodyPart dataPart = new MimeBodyPart(body);
        ICalPart part = new ICalPart(dataPart);
        ICalData data = ICalParser.parse(part);

        assertEquals(1, data.getCalendarData().size());
        assertEquals("PUBLISH", data.getCalendarData().get(0).getMethod().getValue());
        assertEquals("a@example.com", data.getCalendarData().get(0).getOrganizer().getEmail());
    }
}
