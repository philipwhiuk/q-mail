package com.fsck.k9.ical;


import java.util.ArrayList;
import java.util.List;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.parameter.ParticipationLevel;
import biweekly.parameter.ParticipationStatus;
import biweekly.property.Attendee;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ICalDataTest {

    @Test
    public void ICalendar_constructor_storedRequiredAttendeesFromEvent() {
        List<ICalendar> iCalendars = new ArrayList<>();
        ICalendar iCalendar = new ICalendar();
        VEvent event = new VEvent();
        Attendee requiredAttendee = new Attendee("A", "a@b.com");
        requiredAttendee.setParticipationLevel(ParticipationLevel.REQUIRED);
        event.addAttendee(requiredAttendee);
        iCalendar.addEvent(event);
        iCalendars.add(iCalendar);
        ICalData data = new ICalData(iCalendars);

        assertEquals(requiredAttendee, data.getCalendarData().get(0).getRequired()[0]);
    }
    @Test
    public void ICalendar_constructor_storedOptionalAttendeesFromEvent() {
        List<ICalendar> iCalendars = new ArrayList<>();
        ICalendar iCalendar = new ICalendar();
        VEvent event = new VEvent();
        Attendee requiredAttendee = new Attendee("A", "a@b.com");
        requiredAttendee.setParticipationLevel(ParticipationLevel.OPTIONAL);
        event.addAttendee(requiredAttendee);
        iCalendar.addEvent(event);
        iCalendars.add(iCalendar);
        ICalData data = new ICalData(iCalendars);

        assertEquals(requiredAttendee, data.getCalendarData().get(0).getOptional()[0]);
    }

    @Test
    public void ICalendar_constructor_storedFyiAttendeesFromEvent() {
        List<ICalendar> iCalendars = new ArrayList<>();
        ICalendar iCalendar = new ICalendar();
        VEvent event = new VEvent();
        Attendee requiredAttendee = new Attendee("A", "a@b.com");
        requiredAttendee.setParticipationLevel(ParticipationLevel.FYI);
        event.addAttendee(requiredAttendee);
        iCalendar.addEvent(event);
        iCalendars.add(iCalendar);
        ICalData data = new ICalData(iCalendars);

        assertEquals(requiredAttendee, data.getCalendarData().get(0).getFyi()[0]);
    }

    @Test
    public void ICalendar_constructor_storedAcceptedAttendeesFromEvent() {
        List<ICalendar> iCalendars = new ArrayList<>();
        ICalendar iCalendar = new ICalendar();
        VEvent event = new VEvent();
        Attendee requiredAttendee = new Attendee("A", "a@b.com");
        requiredAttendee.setParticipationStatus(ParticipationStatus.ACCEPTED);
        event.addAttendee(requiredAttendee);
        iCalendar.addEvent(event);
        iCalendars.add(iCalendar);
        ICalData data = new ICalData(iCalendars);

        assertEquals(requiredAttendee, data.getCalendarData().get(0).getAccepted()[0]);
    }

    @Test
    public void ICalendar_constructor_storedTentativeAttendeesFromEvent() {
        List<ICalendar> iCalendars = new ArrayList<>();
        ICalendar iCalendar = new ICalendar();
        VEvent event = new VEvent();
        Attendee requiredAttendee = new Attendee("A", "a@b.com");
        requiredAttendee.setParticipationStatus(ParticipationStatus.TENTATIVE);
        event.addAttendee(requiredAttendee);
        iCalendar.addEvent(event);
        iCalendars.add(iCalendar);
        ICalData data = new ICalData(iCalendars);

        assertEquals(requiredAttendee, data.getCalendarData().get(0).getTentative()[0]);
    }

    @Test
    public void ICalendar_constructor_storedDeclinedAttendeesFromEvent() {
        List<ICalendar> iCalendars = new ArrayList<>();
        ICalendar iCalendar = new ICalendar();
        VEvent event = new VEvent();
        Attendee requiredAttendee = new Attendee("A", "a@b.com");
        requiredAttendee.setParticipationStatus(ParticipationStatus.DECLINED);
        event.addAttendee(requiredAttendee);
        iCalendar.addEvent(event);
        iCalendars.add(iCalendar);
        ICalData data = new ICalData(iCalendars);

        assertEquals(requiredAttendee, data.getCalendarData().get(0).getDeclined()[0]);
    }

    @Test
    public void ICalendar_constructor_storedDelegatedAttendeesFromEvent() {
        List<ICalendar> iCalendars = new ArrayList<>();
        ICalendar iCalendar = new ICalendar();
        VEvent event = new VEvent();
        Attendee requiredAttendee = new Attendee("A", "a@b.com");
        requiredAttendee.setParticipationStatus(ParticipationStatus.DELEGATED);
        event.addAttendee(requiredAttendee);
        iCalendar.addEvent(event);
        iCalendars.add(iCalendar);
        ICalData data = new ICalData(iCalendars);

        assertEquals(requiredAttendee, data.getCalendarData().get(0).getDelegated()[0]);
    }
}
