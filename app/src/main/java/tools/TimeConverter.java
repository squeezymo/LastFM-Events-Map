package tools;

import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeConverter {
    public static final String TODAY = "TODAY";
    public static final String TOMORROW = "TOMORROW";

    private static final DateFormat formatDateOnly = new SimpleDateFormat("dd/MM/yyyy");
    private static final DateFormat formatDateOnlyUI = new SimpleDateFormat("EEEE d MMMM yyyy");
    private static final DateFormat formatLastFmResponse = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.US);

    public static long nullifyTimeFromTimestamp(long timestamp) throws ParseException {
        return nullifyTimeFromDate(new Date(timestamp));
    }

    public static long nullifyTimeFromDate(Date date) throws ParseException {
        Date dateTimeNullified = formatDateOnly.parse(formatDateOnly.format(date));
        return dateTimeNullified.getTime();
    }

    public static Date getDateFromLastFmResponse(String date) throws ParseException {
        if (TextUtils.isEmpty(date))
            return null;

        return formatLastFmResponse.parse(date);
    }

    public static String getDateHeaderFromTimestamp(long timestamp) throws ParseException {
        long todayTimestamp = nullifyTimeFromDate(new Date());
        if (timestamp == todayTimestamp)
            return TODAY;

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(todayTimestamp));
        cal.add(Calendar.DAY_OF_WEEK, 1);
        if (timestamp == cal.getTime().getTime())
            return TOMORROW;

        return formatDateOnlyUI.format(new Date(timestamp));
    }
}
