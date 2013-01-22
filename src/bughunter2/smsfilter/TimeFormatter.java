/*
Author: Jelle Geerts

Usage of the works is permitted provided that this instrument is
retained with the works, so that any entity that uses the works is
notified of this instrument.

DISCLAIMER: THE WORKS ARE WITHOUT WARRANTY.
*/

package bughunter2.smsfilter;

import android.content.Context;
import android.text.format.DateUtils;
import android.text.format.Time;

public class TimeFormatter
{
    public static final int SHORT_FORMAT = 0x00000001;
    public static final int FULL_FORMAT  = 0x00000002;

    public static String f(Context context, long timestamp, int flags)
    {
        Time timestampTime = new Time();
        timestampTime.set(timestamp);

        Time now = new Time();
        now.setToNow();

        int dateUtilFlags =
            DateUtils.FORMAT_NO_NOON_MIDNIGHT
            | DateUtils.FORMAT_ABBREV_ALL
            | DateUtils.FORMAT_CAP_AMPM;

        if (timestampTime.year != now.year)
            dateUtilFlags |= DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE;
        else if (timestampTime.yearDay != now.yearDay)
            dateUtilFlags |= DateUtils.FORMAT_SHOW_DATE;
        else
            dateUtilFlags |= DateUtils.FORMAT_SHOW_TIME;

        if ((flags & FULL_FORMAT) != 0)
            dateUtilFlags |= DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME;

        return DateUtils.formatDateTime(context, timestamp, dateUtilFlags);
    }
}
