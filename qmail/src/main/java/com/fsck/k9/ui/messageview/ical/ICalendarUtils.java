package com.fsck.k9.ui.messageview.ical;


import android.content.res.Resources;

import biweekly.util.Frequency;
import biweekly.util.Recurrence;
import com.fsck.k9.R;


public class ICalendarUtils {

    static String buildRule(Recurrence recurrence, Resources resources) {
        Frequency frequency = recurrence.getFrequency();
        switch (frequency) {
            case SECONDLY:
                return resources.getQuantityString(R.plurals.ical_recurrence_second, recurrence.getInterval());
            case MINUTELY:
                return resources.getQuantityString(R.plurals.ical_recurrence_minute, recurrence.getInterval());
            case HOURLY:
                return resources.getQuantityString(R.plurals.ical_recurrence_hourly, recurrence.getInterval());
            case DAILY:
                return resources.getQuantityString(R.plurals.ical_recurrence_daily, recurrence.getInterval());
            case WEEKLY:
                return resources.getQuantityString(R.plurals.ical_recurrence_weekly, recurrence.getInterval());
            case MONTHLY:
                return resources.getQuantityString(R.plurals.ical_recurrence_monthly, recurrence.getInterval());
            case YEARLY:
                return resources.getQuantityString(R.plurals.ical_recurrence_yearly, recurrence.getInterval());
        }
        return "";
    }
}
