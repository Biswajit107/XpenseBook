package com.app.biswajit.xpensebook;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.room.TypeConverter;

public class TimestampConverter {

    private static final String TIME_STAMP_FORMAT = "YYYY-MM-DD HH:MM:SS.SSS";
    private static DateFormat df = new SimpleDateFormat(TIME_STAMP_FORMAT);

    @TypeConverter
    public static Date toDate(Long dateLong){
        return dateLong == null ? null: new Date(dateLong);
    }

    @TypeConverter
    public static Long fromDate(Date date){
        return date == null ? null : date.getTime();
    }
}
