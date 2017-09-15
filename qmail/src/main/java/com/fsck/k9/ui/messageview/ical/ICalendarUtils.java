package com.fsck.k9.ui.messageview.ical;


import android.content.res.Resources;

import biweekly.util.Frequency;
import biweekly.util.Recurrence;
import com.fsck.k9.R;


public class ICalendarUtils {

    static String buildRule(Recurrence recurrence, Resources resources) {
        Frequency frequency = recurrence.getFrequency();
        Integer interval = recurrence.getInterval();
        switch (frequency) {
            case SECONDLY:
                return resources.getQuantityString(R.plurals.ical_recurrence_second, interval, interval);
            case MINUTELY:
                return resources.getQuantityString(R.plurals.ical_recurrence_minute, interval, interval);
            case HOURLY:
                return resources.getQuantityString(R.plurals.ical_recurrence_hourly, interval, interval);
            case DAILY:
                return resources.getQuantityString(R.plurals.ical_recurrence_daily, interval, interval);
            case WEEKLY:
                return resources.getQuantityString(R.plurals.ical_recurrence_weekly, interval, interval);
            case MONTHLY:
                return resources.getQuantityString(R.plurals.ical_recurrence_monthly, interval, interval);
            case YEARLY:
                return resources.getQuantityString(R.plurals.ical_recurrence_yearly, interval, interval);
        }
        return "";
    }
}
